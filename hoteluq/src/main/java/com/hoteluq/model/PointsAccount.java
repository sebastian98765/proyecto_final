package com.hoteluq.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PointsAccount {

    // Lotes de puntos acumulados
    private final List<PointsLot> lots = new ArrayList<>();

    // Reservas cuyos puntos ya fueron revocados
    private final Set<String> reservasRevocadas = new HashSet<>();

    // Reservas cuyos puntos ya fueron acreditados
    private final Set<String> reservasAcreditadas = new HashSet<>();

    // Puntos utilizados para compensar deudas anteriores
    private final Map<String, Integer> compensacionesPorReserva =
            new HashMap<>();

    // Deuda de puntos del cliente
    private int deudaPuntos = 0;

    /**
     * Registra los puntos utilizados por cada reserva
     * y el lote del que fueron descontados.
     */
    private final Map<String, Map<PointsLot, Integer>> redencionesPorReserva =
            new HashMap<>();

    private final Set<String> redencionesRestauradas =
            new HashSet<>();

    /**
     * Acumula puntos normales.
     * Primero compensa cualquier deuda pendiente.
     */
    public synchronized void earn(int points, LocalDate date) {

        Objects.requireNonNull(date, "La fecha es obligatoria");

        if (points <= 0) {
            return;
        }

        int compensacion = Math.min(points, deudaPuntos);

        deudaPuntos -= compensacion;

        int restantes = points - compensacion;

        if (restantes > 0) {
            lots.add(new PointsLot(restantes, date));
        }
    }

    /**
     * Acumula puntos asociados a una reserva específica.
     *
     * Registra también los puntos utilizados para
     * compensar deudas anteriores.
     */
    public synchronized void earnForReservation(
            int points,
            LocalDate date,
            String reservationId
    ) {

        Objects.requireNonNull(date, "La fecha es obligatoria");

        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException(
                    "La reserva es obligatoria"
            );
        }

        if (points <= 0) {
            return;
        }

        if (reservasAcreditadas.contains(reservationId)
                || reservasRevocadas.contains(reservationId)) {

            throw new IllegalStateException(
                    "Los puntos de esta reserva ya fueron procesados"
            );
        }

        int compensacion = Math.min(points, deudaPuntos);

        deudaPuntos -= compensacion;

        int restantes = points - compensacion;

        compensacionesPorReserva.put(
                reservationId,
                compensacion
        );

        reservasAcreditadas.add(reservationId);

        if (restantes > 0) {
            lots.add(new PointsLot(
                    restantes,
                    date,
                    reservationId
            ));
        }
    }

    /**
     * Consulta los puntos disponibles para redimir.
     *
     * Si existe deuda, no permite utilizar puntos.
     */
    public synchronized int available(LocalDate date) {

        Objects.requireNonNull(date, "La fecha es obligatoria");

        if (deudaPuntos > 0) {
            return 0;
        }

        return lots.stream()
                .filter(lot -> lot.isValid(date))
                .mapToInt(PointsLot::getRemaining)
                .sum();
    }

    /**
     * Consulta el saldo neto.
     *
     * Puede ser negativo cuando existe una deuda.
     */
    public synchronized int balance(LocalDate date) {

        Objects.requireNonNull(date, "La fecha es obligatoria");

        int vigentes = lots.stream()
                .filter(lot -> lot.isValid(date))
                .mapToInt(PointsLot::getRemaining)
                .sum();

        return vigentes - deudaPuntos;
    }

    /**
     * Consulta la deuda de puntos.
     */
    public synchronized int getDeudaPuntos() {
        return deudaPuntos;
    }

    /**
     * Redime puntos.
     *
     * Primero utiliza los lotes que vencen antes.
     */
    public synchronized void redeem(int points, LocalDate date) {

        Objects.requireNonNull(date, "La fecha es obligatoria");

        if (points <= 0 || available(date) < points) {
            throw new IllegalStateException(
                    "Puntos insuficientes o vencidos"
            );
        }

        int restantes = points;

        List<PointsLot> lotesDisponibles = lots.stream()
                .filter(lot -> lot.isValid(date))
                .sorted(
                        Comparator.comparing(
                                PointsLot::getExpires
                        )
                )
                .toList();

        for (PointsLot lot : lotesDisponibles) {

            int cantidad = Math.min(
                    restantes,
                    lot.getRemaining()
            );

            lot.consume(cantidad);

            restantes -= cantidad;

            if (restantes == 0) {
                break;
            }
        }
    }

    /**
     * Revoca una cantidad general de puntos.
     *
     * Si no hay suficientes puntos disponibles,
     * genera una deuda.
     */
    public synchronized void revoke(int points, LocalDate date) {

        Objects.requireNonNull(date, "La fecha es obligatoria");

        if (points <= 0) {
            return;
        }

        int restantes = points;

        List<PointsLot> lotesDisponibles = lots.stream()
                .filter(lot -> lot.isValid(date))
                .sorted(
                        Comparator.comparing(
                                PointsLot::getExpires
                        )
                )
                .toList();

        for (PointsLot lot : lotesDisponibles) {

            int cantidad = Math.min(
                    restantes,
                    lot.getRemaining()
            );

            lot.consume(cantidad);

            restantes -= cantidad;

            if (restantes == 0) {
                break;
            }
        }

        deudaPuntos += restantes;
    }

    /**
     * Revoca los puntos generados por una reserva.
     *
     * También revierte los puntos que esa reserva
     * utilizó para compensar una deuda anterior.
     *
     * No consume directamente puntos de otros lotes.
     */
    public synchronized void revokeReservation(
            String reservationId,
            LocalDate date
    ) {

        Objects.requireNonNull(date, "La fecha es obligatoria");

        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException(
                    "La reserva es obligatoria"
            );
        }

        if (reservasRevocadas.contains(reservationId)) {
            throw new IllegalStateException(
                    "Los puntos de esta reserva ya fueron revocados"
            );
        }

        if (!reservasAcreditadas.contains(reservationId)) {
            throw new IllegalStateException(
                    "No existen puntos acreditados para esta reserva"
            );
        }

        List<PointsLot> asociados = lots.stream()
                .filter(lot ->
                        reservationId.equals(
                                lot.getReservationId()
                        )
                )
                .toList();

        int puntosConsumidos = 0;

        for (PointsLot lot : asociados) {

            puntosConsumidos += lot.getOriginal()
                    - lot.getRemaining();

            if (lot.getRemaining() > 0) {
                lot.consume(lot.getRemaining());
            }
        }

        int compensacionAnterior =
                compensacionesPorReserva.getOrDefault(
                        reservationId,
                        0
                );

        deudaPuntos += puntosConsumidos
                + compensacionAnterior;

        reservasRevocadas.add(reservationId);
    }

    /**
     * Devuelve puntos al cliente.
     *
     * Primero compensa cualquier deuda pendiente.
     */
    public synchronized void restore(int points, LocalDate date) {

        Objects.requireNonNull(date, "La fecha es obligatoria");

        if (points < 0) {
            throw new IllegalArgumentException(
                    "Los puntos no pueden ser negativos"
            );
        }

        if (points == 0) {
            return;
        }

        earn(points, date);
    }

    /**
     * Devuelve una copia de la lista de lotes.
     */
    public synchronized List<PointsLot> getLots() {
        return List.copyOf(lots);
    }

    /**
     * Redime puntos y registra los lotes utilizados
     * para una reserva específica.
     */
    public synchronized void redeemForReservation(
            int points,
            LocalDate date,
            String reservationId
    ) {
        Objects.requireNonNull(date, "La fecha es obligatoria");

        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException(
                    "La reserva es obligatoria"
            );
        }

        if (redencionesPorReserva.containsKey(reservationId)) {
            throw new IllegalStateException(
                    "Esta reserva ya tiene una redención registrada"
            );
        }

        if (points <= 0 || available(date) < points) {
            throw new IllegalStateException(
                    "Puntos insuficientes o vencidos"
            );
        }

        int restantes = points;

        Map<PointsLot, Integer> consumidos = new HashMap<>();

        List<PointsLot> disponibles = lots.stream()
                .filter(lot -> lot.isValid(date))
                .sorted(Comparator.comparing(PointsLot::getExpires))
                .toList();

        for (PointsLot lot : disponibles) {

            int cantidad = Math.min(restantes, lot.getRemaining());

            if (cantidad > 0) {
                lot.consume(cantidad);
                consumidos.put(lot, cantidad);
                restantes -= cantidad;
            }

            if (restantes == 0) {
                break;
            }
        }

        redencionesPorReserva.put(reservationId, consumidos);
    }

    /**
     * Devuelve los puntos utilizados en una reserva,
     * respetando el vencimiento de sus lotes originales.
     *
     * Permite devoluciones parciales y evita que una misma
     * reserva reciba puntos más de una vez.
     */
    public synchronized void restoreReservationPoints(
            String reservationId,
            int points,
            LocalDate date
    ) {
        Objects.requireNonNull(date, "La fecha es obligatoria");

        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException(
                    "La reserva es obligatoria"
            );
        }

        if (points < 0) {
            throw new IllegalArgumentException(
                    "Los puntos no pueden ser negativos"
            );
        }

        if (redencionesRestauradas.contains(reservationId)) {
            throw new IllegalStateException(
                    "Los puntos de esta reserva ya fueron procesados"
            );
        }

        Map<PointsLot, Integer> consumidos =
                redencionesPorReserva.get(reservationId);

        if (consumidos == null) {
            throw new IllegalStateException(
                    "No existe una redención registrada para esta reserva"
            );
        }

        int totalRedimido = consumidos.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (points > totalRedimido) {
            throw new IllegalArgumentException(
                    "No se pueden devolver más puntos de los utilizados"
            );
        }

        int restantes = points;

        // Restaurar primero los lotes con vencimiento más próximo.
        List<Map.Entry<PointsLot, Integer>> entradas =
                consumidos.entrySet()
                        .stream()
                        .sorted(Comparator.comparing(
                                entrada -> entrada.getKey().getExpires()
                        ))
                        .toList();

        for (Map.Entry<PointsLot, Integer> entrada : entradas) {

            if (restantes == 0) {
                break;
            }

            PointsLot lote = entrada.getKey();

            // Los puntos vencidos no vuelven a estar disponibles.
            if (!date.isBefore(lote.getExpires())) {
                continue;
            }

            int cantidad = Math.min(
                    restantes,
                    entrada.getValue()
            );

            // No superar la capacidad original del lote.
            int capacidad = lote.getOriginal()
                    - lote.getRemaining();

            cantidad = Math.min(cantidad, capacidad);

            if (cantidad > 0) {
                lote.restore(cantidad);
                restantes -= cantidad;
            }
        }

        redencionesRestauradas.add(reservationId);
    }
}