package com.hoteluq.service;

import com.hoteluq.exception.BusinessException;
import com.hoteluq.model.Reservation;
import com.hoteluq.model.Room;
import com.hoteluq.model.Wallet;
import com.hoteluq.model.enums.ReservationStatus;
import com.hoteluq.pattern.behavioral.Observer.ReservationObserver;
import com.hoteluq.pattern.behavioral.strategy.CancellationPolicy;
import com.hoteluq.pattern.behavioral.strategy.HotelCancellationPolicy;
import com.hoteluq.pattern.structural.PaymentGateway;
import com.hoteluq.repository.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Reglas de negocio y coordinación del ciclo de vida de las reservas. */
public class ReservationService {
    private final Repository<Reservation, String> reservations;
    private final List<ReservationObserver> observers = new ArrayList<>();

    public ReservationService(Repository<Reservation, String> reservations) {
        this.reservations = Objects.requireNonNull(reservations);
    }

    public synchronized void addObserver(ReservationObserver observer) {
        observers.add(Objects.requireNonNull(observer));
    }

    /**
     * Confirma de forma sincronizada para evitar que dos solicitudes simultáneas
     * ocupen la misma habitación en intervalos que se solapan.
     */
    public synchronized void confirmReservation(Reservation reservation, PaymentGateway payment) {
        Objects.requireNonNull(reservation, "La reserva es obligatoria");
        Objects.requireNonNull(payment, "El medio de pago es obligatorio");

        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new BusinessException("Solo se pueden confirmar reservas pendientes");
        }
        if (!reservation.getRoom().isActive()) {
            throw new BusinessException("La habitación está inactiva");
        }
        if (!isAvailable(reservation.getRoom(), reservation.getCheckIn(), reservation.getCheckOut())) {
            throw new BusinessException("La habitación no está disponible para esas fechas");
        }

        boolean paid = payment.pay(reservation.getTotal(), "Reserva HotelUQ " + reservation.getId());
        if (!paid) {
            throw new BusinessException("El pago no fue aprobado; la reserva no se confirmó");
        }

        reservation.confirm();
        reservations.save(reservation);
        notifyObservers(reservation, "RESERVA CONFIRMADA");
    }

    /**
     * Las fechas se manejan como intervalos [entrada, salida): una salida el mismo
     * día de la siguiente entrada no representa solapamiento.
     */
    public synchronized boolean isAvailable(Room room, LocalDate in, LocalDate out) {
        Objects.requireNonNull(room, "La habitación es obligatoria");
        if (in == null || out == null || !out.isAfter(in) || !room.isActive()) {
            return false;
        }

        return reservations.findAll().stream()
                .filter(existing -> existing.getRoom().getNumber().equals(room.getNumber()))
                .filter(existing -> existing.getStatus() != ReservationStatus.CANCELLED)
                .noneMatch(existing -> in.isBefore(existing.getCheckOut())
                        && out.isAfter(existing.getCheckIn()));
    }

    public List<Reservation> all() {
        return reservations.findAll();
    }

    public synchronized void cancel(Reservation reservation, Wallet wallet,
                                    CancellationPolicy policy, boolean byHotel) {
        Objects.requireNonNull(reservation, "La reserva es obligatoria");
        Objects.requireNonNull(wallet, "La billetera es obligatoria");
        Objects.requireNonNull(policy, "La política de cancelación es obligatoria");

        if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw new BusinessException("Solo se cancelan reservas confirmadas");
        }

        double refund = (byHotel ? new HotelCancellationPolicy() : policy)
                .refund(reservation.getTotal(), reservation.getCheckIn().atStartOfDay(), LocalDateTime.now());
        if (!Double.isFinite(refund) || refund < 0 || refund > reservation.getTotal()) {
            throw new BusinessException("La política produjo un valor de reembolso inválido");
        }

        reservation.cancel();
        if (refund > 0) {
            reservation.markRefunded();
            wallet.refund(refund, "Reembolso reserva " + reservation.getId());
        }
        notifyObservers(reservation, "RESERVA CANCELADA; reembolso: $" + refund);
    }

    private void notifyObservers(Reservation reservation, String event) {
        observers.forEach(observer -> observer.onReservationChanged(reservation, event));
    }
}
