package com.hoteluq.model;

import java.time.LocalDate;
import java.util.Objects;

public class PointsLot {

    private final int original;
    private int remaining;

    private final LocalDate earned;
    private final LocalDate expires;

    private final String reservationId;

    // Constructor original: conserva compatibilidad
    public PointsLot(int points, LocalDate earned) {
        this(points, earned, null);
    }

    // Constructor para asociar puntos a una reserva
    public PointsLot(
            int points,
            LocalDate earned,
            String reservationId
    ) {
        if (points <= 0) {
            throw new IllegalArgumentException(
                    "Los puntos deben ser positivos"
            );
        }

        this.earned = Objects.requireNonNull(
                earned, "La fecha es obligatoria"
        );

        this.original = points;
        this.remaining = points;
        this.expires = earned.plusYears(1);
        this.reservationId = reservationId;
    }

    public int getOriginal() {
        return original;
    }

    public int getRemaining() {
        return remaining;
    }

    public LocalDate getEarned() {
        return earned;
    }

    public LocalDate getExpires() {
        return expires;
    }

    public String getReservationId() {
        return reservationId;
    }

    public boolean isValid(LocalDate date) {
        Objects.requireNonNull(date);
        return date.isBefore(expires) && remaining > 0;
    }

    public void consume(int points) {
        if (points < 0 || points > remaining) {
            throw new IllegalArgumentException(
                    "Cantidad de puntos inválida"
            );
        }

        remaining -= points;
    }

    /**
     * Restaura puntos consumidos de este lote,
     * sin modificar su fecha original de vencimiento.
     */
    public void restore(int points) {

        if (points < 0) {
            throw new IllegalArgumentException(
                    "Los puntos a restaurar no pueden ser negativos"
            );
        }

        if (remaining + points > original) {
            throw new IllegalStateException(
                    "No se pueden restaurar más puntos de los originales"
            );
        }

        remaining += points;
    }
}