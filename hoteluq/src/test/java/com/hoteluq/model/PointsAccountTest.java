package com.hoteluq.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PointsAccountTest {

    @Test
    void cancelarReservaDebeRevertirPuntosQueCompensaronDeuda() {

        PointsAccount cuenta = new PointsAccount();
        LocalDate hoy = LocalDate.of(2026, 11, 19);

        // El cliente adquiere una deuda de 30 puntos.
        cuenta.revoke(30, hoy);

        assertEquals(-30, cuenta.balance(hoy));

        // Una nueva reserva genera 50 puntos.
        cuenta.earnForReservation(50, hoy, "RESERVA-002");

        // 30 compensan la deuda y quedan 20 disponibles.
        assertEquals(20, cuenta.balance(hoy));

        // Se cancela la reserva que generó los 50 puntos.
        cuenta.revokeReservation("RESERVA-002", hoy);

        // Debe recuperarse la deuda original de 30 puntos.
        assertEquals(-30, cuenta.balance(hoy));
    }

    @Test
    void noPermiteRevocarDosVecesLaMismaReserva() {

        PointsAccount cuenta = new PointsAccount();
        LocalDate hoy = LocalDate.of(2026, 11, 19);

        cuenta.earnForReservation(50, hoy, "RESERVA-001");

        cuenta.revokeReservation("RESERVA-001", hoy);

        assertThrows(
                IllegalStateException.class,
                () -> cuenta.revokeReservation("RESERVA-001", hoy)
        );
    }

    @Test
    void cancelarUnaReservaNoConsumePuntosDeOtra() {

        PointsAccount cuenta = new PointsAccount();
        LocalDate hoy = LocalDate.of(2026, 11, 19);

        cuenta.earnForReservation(50, hoy, "RESERVA-001");
        cuenta.earnForReservation(40, hoy, "RESERVA-002");

        cuenta.redeem(30, hoy);

        cuenta.revokeReservation("RESERVA-001", hoy);

        // La reserva 001 tenía 20 puntos restantes.
        // Los otros 30 utilizados generan una deuda.
        // La reserva 002 conserva sus 40 puntos.
        assertEquals(40, cuenta.getLots().stream()
                .filter(lot -> "RESERVA-002".equals(
                        lot.getReservationId()))
                .mapToInt(PointsLot::getRemaining)
                .sum());

        assertEquals(30, cuenta.getDeudaPuntos());

        // Saldo neto: 40 - 30 = 10.
        assertEquals(10, cuenta.balance(hoy));
    }
}