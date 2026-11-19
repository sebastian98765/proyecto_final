package com.hoteluq.service;

import com.hoteluq.model.Reservation;
import com.hoteluq.model.User;
import com.hoteluq.model.enums.ReservationStatus;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Servicio de fidelización de clientes de HotelUQ.
 *
 * Reglas:
 * - 1 punto por cada $10.000 COP pagados.
 * - Los puntos tienen una vigencia de un año.
 * - Una misma reserva no puede generar puntos dos veces.
 */
public class LoyaltyService {

    private static final double PESOS_POR_PUNTO = 10_000.0;
    private static final double DESCUENTO_POR_PUNTO = 1_000.0;

    private final Set<String> reservasAcreditadas = new HashSet<>();
    public int calcularPuntos(double valorPagado) {
        if (!Double.isFinite(valorPagado) || valorPagado < 0) {
            throw new IllegalArgumentException(
                    "El valor pagado debe ser válido y no negativo"
            );
        }

        return (int) Math.floor(valorPagado / PESOS_POR_PUNTO);
    }

    public synchronized int acreditarPuntos(Reservation reserva) {

        Objects.requireNonNull(
                reserva, "La reserva es obligatoria"
        );

        if (reserva.getStatus() != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException(
                    "Solo las reservas confirmadas generan puntos"
            );
        }

        if (reservasAcreditadas.contains(reserva.getId())) {
            return 0;
        }

        int puntos = calcularPuntos(reserva.getTotal());

        if (puntos > 0) {
            reserva.getCustomer()
                    .getPointsAccount()
                    .earnForReservation(
                            puntos,
                            LocalDate.now(),
                            reserva.getId()
                    );
        }

        reservasAcreditadas.add(reserva.getId());

        return puntos;
    }

    public int consultarPuntos(User cliente) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio");

        return cliente.getPointsAccount()
                .available(LocalDate.now());
    }

    public double calcularDescuento(int puntos) {
        if (puntos <= 0) {
            throw new IllegalArgumentException(
                    "Los puntos deben ser mayores que cero"
            );
        }

        return puntos * DESCUENTO_POR_PUNTO;
    }

    public synchronized void redimirPuntos(
            User cliente,
            int puntos,
            double valorReserva
    ) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio");

        if (!Double.isFinite(valorReserva) || valorReserva <= 0) {
            throw new IllegalArgumentException(
                    "El valor de la reserva debe ser válido y positivo"
            );
        }

        if (puntos <= 0) {
            throw new IllegalArgumentException(
                    "Debes utilizar al menos un punto"
            );
        }

        double descuento = calcularDescuento(puntos);

        if (descuento > valorReserva - 1_000) {
            throw new IllegalArgumentException(
                    "Debes pagar al menos $1.000 con la tarjeta virtual"
            );
        }

        if (consultarPuntos(cliente) < puntos) {
            throw new IllegalStateException(
                    "No tienes suficientes puntos disponibles"
            );
        }

        cliente.getPointsAccount().redeem(puntos, LocalDate.now());
    }

    /**
     * Calcula cuántos puntos deben devolverse según
     * el porcentaje de reembolso aprobado.
     */
    public int calcularPuntosADevolver(
            int puntosUtilizados,
            double totalPagado,
            double reembolso
    ) {
        if (puntosUtilizados < 0
                || !Double.isFinite(totalPagado)
                || !Double.isFinite(reembolso)
                || totalPagado <= 0
                || reembolso < 0
                || reembolso > totalPagado) {
            throw new IllegalArgumentException(
                    "Los valores del reembolso no son válidos"
            );
        }

        double proporcion = reembolso / totalPagado;

        return (int) Math.floor(
                puntosUtilizados * proporcion
        );
    }

}