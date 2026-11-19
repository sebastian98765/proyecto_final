package com.hoteluq.service;

import com.hoteluq.exception.BusinessException;
import com.hoteluq.model.PointsAccount;
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

public class ReservationService {

    private final Repository<Reservation, String> reservations;

    private final List<ReservationObserver> observers =
            new ArrayList<>();

    private final LoyaltyService loyaltyService =
            new LoyaltyService();

    public ReservationService(
            Repository<Reservation, String> reservations
    ) {
        this.reservations = Objects.requireNonNull(
                reservations,
                "El repositorio es obligatorio"
        );
    }

    public synchronized void addObserver(
            ReservationObserver observer
    ) {
        observers.add(
                Objects.requireNonNull(observer)
        );
    }

    /**
     * Confirma una reserva sin utilizar puntos.
     */
    public synchronized void confirmReservation(
            Reservation reservation,
            PaymentGateway payment
    ) {
        confirmReservation(reservation, payment, 0);
    }

    /**
     * Confirma una reserva utilizando opcionalmente
     * puntos de fidelización.
     */
    public synchronized void confirmReservation(
            Reservation reservation,
            PaymentGateway payment,
            int puntosUtilizados
    ) {

        Objects.requireNonNull(
                reservation,
                "La reserva es obligatoria"
        );

        Objects.requireNonNull(
                payment,
                "El medio de pago es obligatorio"
        );

        if (reservation.getStatus()
                != ReservationStatus.PENDING) {

            throw new BusinessException(
                    "Solo se pueden confirmar reservas pendientes"
            );
        }

        if (!reservation.getRoom().isActive()) {

            throw new BusinessException(
                    "La habitación está inactiva"
            );
        }

        if (!isAvailable(
                reservation.getRoom(),
                reservation.getCheckIn(),
                reservation.getCheckOut()
        )) {

            throw new BusinessException(
                    "La habitación no está disponible para esas fechas"
            );
        }

        if (puntosUtilizados < 0) {

            throw new BusinessException(
                    "Los puntos utilizados no pueden ser negativos"
            );
        }

        double totalPagar = reservation.getTotal();

        if (!Double.isFinite(totalPagar)
                || totalPagar < 1_000) {

            throw new BusinessException(
                    "La reserva debe tener un pago mínimo de $1.000"
            );
        }

        boolean paid = payment.pay(
                totalPagar,
                "Reserva HotelUQ " + reservation.getId()
        );

        if (!paid) {

            throw new BusinessException(
                    "El pago no fue aprobado; la reserva no se confirmó"
            );
        }

        // Consumir los puntos una sola vez, tras un pago aprobado,
        // registrando los lotes de origen para una posible devolución.
        if (puntosUtilizados > 0) {
            reservation.getCustomer().getPointsAccount()
                    .redeemForReservation(puntosUtilizados, LocalDate.now(), reservation.getId());
        }

        reservation.setPuntosUtilizados(
                puntosUtilizados
        );

        reservation.confirm();

        reservations.save(reservation);

        int puntosGanados =
                loyaltyService.acreditarPuntos(reservation);

        reservation.setPuntosGanados(
                puntosGanados
        );

        notifyObservers(
                reservation,
                "RESERVA CONFIRMADA - Puntos ganados: "
                        + puntosGanados
                        + " - Puntos utilizados: "
                        + puntosUtilizados
        );
    }

    /**
     * Comprueba la disponibilidad de una habitación.
     */
    public synchronized boolean isAvailable(
            Room room,
            LocalDate in,
            LocalDate out
    ) {

        Objects.requireNonNull(
                room,
                "La habitación es obligatoria"
        );

        if (in == null
                || out == null
                || !out.isAfter(in)
                || !room.isActive()) {

            return false;
        }

        return reservations.findAll()
                .stream()
                .filter(existing ->
                        existing.getRoom()
                                .getNumber()
                                .equals(room.getNumber())
                )
                .filter(existing ->
                        existing.getStatus()
                                != ReservationStatus.CANCELLED
                )
                .noneMatch(existing ->
                        in.isBefore(existing.getCheckOut())
                                && out.isAfter(existing.getCheckIn())
                );
    }

    /**
     * Obtiene todas las reservas.
     */
    public List<Reservation> all() {
        return reservations.findAll();
    }

    /**
     * Cancela una reserva.
     *
     * - Calcula el reembolso.
     * - Revoca los puntos ganados.
     * - Devuelve proporcionalmente los puntos utilizados.
     * - Reembolsa el dinero.
     * - Cambia el estado de la reserva.
     */
    public synchronized void cancel(
            Reservation reservation,
            Wallet wallet,
            CancellationPolicy policy,
            boolean byHotel
    ) {

        Objects.requireNonNull(
                reservation,
                "La reserva es obligatoria"
        );

        Objects.requireNonNull(
                wallet,
                "La billetera es obligatoria"
        );

        Objects.requireNonNull(
                policy,
                "La política de cancelación es obligatoria"
        );

        // Evitar cancelar una reserva más de una vez.
        if (reservation.getStatus()
                != ReservationStatus.CONFIRMED) {

            throw new BusinessException(
                    "Solo se cancelan reservas confirmadas"
            );
        }

        // Calcular el reembolso.
        CancellationPolicy politicaAplicada =
                byHotel
                        ? new HotelCancellationPolicy()
                        : policy;

        double totalPagado = reservation.getTotal();

        double refund = politicaAplicada.refund(
                totalPagado,
                reservation.getCheckIn().atStartOfDay(),
                LocalDateTime.now()
        );

        if (!Double.isFinite(refund)
                || refund < 0
                || refund > totalPagado) {

            throw new BusinessException(
                    "La política produjo un valor de reembolso inválido"
            );
        }

        PointsAccount cuenta =
                reservation.getCustomer().getPointsAccount();

        LocalDate hoy = LocalDate.now();

        int puntosGanados =
                reservation.getPuntosGanados();

        int puntosUtilizados =
                reservation.getPuntosUtilizados();

        // Calcular los puntos que deben devolverse.
        int puntosADevolver = 0;

        if (puntosUtilizados > 0) {

            puntosADevolver =
                    loyaltyService.calcularPuntosADevolver(
                            puntosUtilizados,
                            totalPagado,
                            refund
                    );
        }

        // Retirar los puntos ganados por esta reserva.
        if (puntosGanados > 0) {

            cuenta.revokeReservation(
                    reservation.getId(),
                    hoy
            );
        }

        // Devolver los puntos utilizados.
        if (puntosUtilizados > 0) {

            cuenta.restoreReservationPoints(
                    reservation.getId(),
                    puntosADevolver,
                    hoy
            );
        }

        // Devolver el dinero.
        if (refund > 0) {

            wallet.refund(
                    refund,
                    "Reembolso reserva "
                            + reservation.getId()
            );

            reservation.markRefunded();
        }

        // Cambiar el estado de la reserva.
        reservation.cancel();

        notifyObservers(
                reservation,
                "RESERVA CANCELADA"
                        + " - Reembolso: $" + refund
                        + " - Puntos revocados: " + puntosGanados
                        + " - Puntos devueltos: " + puntosADevolver
        );
    }

    /**
     * Notifica a los observadores.
     */
    private void notifyObservers(
            Reservation reservation,
            String event
    ) {

        observers.forEach(
                observer ->
                        observer.onReservationChanged(
                                reservation,
                                event
                        )
        );
    }
}