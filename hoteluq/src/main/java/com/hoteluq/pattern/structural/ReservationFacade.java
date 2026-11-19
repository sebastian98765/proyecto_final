package com.hoteluq.pattern.structural;

import com.hoteluq.model.AdditionalService;
import com.hoteluq.model.Reservation;
import com.hoteluq.model.Room;
import com.hoteluq.model.User;
import com.hoteluq.pattern.behavioral.strategy.PromotionStrategy;
import com.hoteluq.pattern.creational.ReservationBuilder;
import com.hoteluq.service.ReservationService;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Fachada del proceso de reserva. Coordina Builder, Strategy, el servicio
 * de reservas y el adaptador de pagos sin trasladar esa complejidad al controlador.
 */
public class ReservationFacade {
    private final ReservationService service;

    public ReservationFacade(ReservationService service) {
        this.service = Objects.requireNonNull(service, "El servicio de reservas es obligatorio");
    }

    /** Reserva sin promoción, conservando la firma utilizada por el controlador. */
    public Reservation book(User user, Room room, LocalDate in, LocalDate out,
                            List<AdditionalService> extras, PaymentGateway payment) {
        return book(user, room, in, out, extras, payment, null);
    }

    /** Reserva aplicando opcionalmente una estrategia de promoción. */
    public Reservation book(User user, Room room, LocalDate in, LocalDate out,
                            List<AdditionalService> extras, PaymentGateway payment,
                            PromotionStrategy promotion) {
        Objects.requireNonNull(user, "El cliente es obligatorio");
        Objects.requireNonNull(room, "La habitación es obligatoria");
        Objects.requireNonNull(in, "La fecha de entrada es obligatoria");
        Objects.requireNonNull(out, "La fecha de salida es obligatoria");
        Objects.requireNonNull(extras, "La lista de servicios es obligatoria");
        Objects.requireNonNull(payment, "El medio de pago es obligatorio");

        if (!room.isActive()) {
            throw new IllegalStateException("La habitación está inactiva y no se puede reservar");
        }
        if (!out.isAfter(in)) {
            throw new IllegalArgumentException("La fecha de salida debe ser posterior a la entrada");
        }
        if (extras.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("La reserva contiene un servicio inválido");
        }
        if (extras.stream().anyMatch(extra -> !extra.available())) {
            throw new IllegalStateException("Hay servicios no disponibles");
        }

        ReservationBuilder builder = new ReservationBuilder()
                .customer(user)
                .room(room)
                .dates(in, out);
        extras.forEach(builder::addService);
        Reservation draft = builder.build();

        double discount = promotion == null ? 0.0 : promotion.apply(draft);
        if (!Double.isFinite(discount) || discount < 0 || discount > draft.getTotal()) {
            throw new IllegalArgumentException("La estrategia produjo un descuento inválido");
        }

        Reservation reservation = discount == 0.0
                ? draft
                : new Reservation(user, room, in, out, extras, draft.getTotal() - discount);

        service.confirmReservation(reservation, payment);
        return reservation;
    }
}
