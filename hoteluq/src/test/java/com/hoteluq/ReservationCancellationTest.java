package com.hoteluq;

import com.hoteluq.exception.BusinessException;
import com.hoteluq.model.Reservation;
import com.hoteluq.model.Room;
import com.hoteluq.model.User;
import com.hoteluq.model.enums.ReservationStatus;
import com.hoteluq.model.enums.RoomType;
import com.hoteluq.pattern.behavioral.strategy.HotelCancellationPolicy;
import com.hoteluq.pattern.creational.RoomFactory;
import com.hoteluq.pattern.structural.WalletPaymentAdapter;
import com.hoteluq.repository.ReservationRepository;
import com.hoteluq.repository.UserRepository;
import com.hoteluq.security.PasswordHasher;
import com.hoteluq.service.AuthService;
import com.hoteluq.service.ReservationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationCancellationTest {

    private ReservationService reservationService;
    private User cliente;
    private Room habitacion;

    @BeforeEach
    void setUp() {

        reservationService = new ReservationService(
                new ReservationRepository()
        );

        AuthService authService = new AuthService(
                new UserRepository(),
                new PasswordHasher()
        );

        cliente = authService.registerClient(
                "Cliente de prueba",
                "cliente@test.com",
                "ClaveSegura9".toCharArray()
        );

        habitacion = new RoomFactory().create(
                RoomType.STANDARD,
                "501"
        );

        cliente.getWallet().topUp(
                2_000_000,
                "Recarga inicial"
        );
    }

    /**
     * Comprueba que el hotel devuelve el dinero
     * cuando cancela una reserva.
     */
    @Test
    void cancelarPorHotelDevuelveDinero() {

        Reservation reserva = crearReserva();

        double saldoAntes = cliente.getWallet().getBalance();

        reservationService.cancel(
                reserva,
                cliente.getWallet(),
                new HotelCancellationPolicy(),
                true
        );

        assertEquals(
                saldoAntes + reserva.getTotal(),
                cliente.getWallet().getBalance(),
                0.001
        );

        assertEquals(
                ReservationStatus.CANCELLED,
                reserva.getStatus()
        );
    }

    /**
     * Comprueba que se revocan los puntos ganados.
     */
    @Test
    void cancelarReservaRevocaPuntosGanados() {

        Reservation reserva = crearReserva();

        int puntosGanados = reserva.getPuntosGanados();

        assertTrue(puntosGanados > 0);

        reservationService.cancel(
                reserva,
                cliente.getWallet(),
                new HotelCancellationPolicy(),
                true
        );

        assertEquals(
                0,
                cliente.getPointsAccount().available(
                        LocalDate.now()
                )
        );

        assertEquals(
                ReservationStatus.CANCELLED,
                reserva.getStatus()
        );
    }

    /**
     * Comprueba que no se puede cancelar
     * dos veces la misma reserva.
     */
    @Test
    void noPermiteCancelarDosVeces() {

        Reservation reserva = crearReserva();

        reservationService.cancel(
                reserva,
                cliente.getWallet(),
                new HotelCancellationPolicy(),
                true
        );

        assertThrows(
                BusinessException.class,
                () -> reservationService.cancel(
                        reserva,
                        cliente.getWallet(),
                        new HotelCancellationPolicy(),
                        true
                )
        );
    }

    /**
     * Crea una reserva confirmada para las pruebas.
     */
    private Reservation crearReserva() {

        Reservation reserva = new Reservation(
                cliente,
                habitacion,
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                List.of(),
                400_000
        );

        reservationService.confirmReservation(
                reserva,
                new WalletPaymentAdapter(
                        cliente.getWallet()
                )
        );

        return reserva;
    }

    @Test
    void cancelarReservaDevuelvePuntosUtilizados() {

        // El cliente recibe 100 puntos inicialmente.
        cliente.getPointsAccount().earn(
                100,
                LocalDate.now()
        );

        // Creamos una reserva con un pago de $350.000.
        Reservation reserva = new Reservation(
                cliente,
                habitacion,
                LocalDate.now().plusDays(15),
                LocalDate.now().plusDays(17),
                List.of(),
                350_000
        );

        // Confirmamos utilizando 50 puntos.
        reservationService.confirmReservation(
                reserva,
                new WalletPaymentAdapter(cliente.getWallet()),
                50
        );

        // 100 iniciales - 50 utilizados + 35 ganados.
        assertEquals(
                85,
                cliente.getPointsAccount().available(LocalDate.now())
        );

        // El hotel cancela la reserva.
        reservationService.cancel(
                reserva,
                cliente.getWallet(),
                new HotelCancellationPolicy(),
                true
        );

        // Deben recuperarse los 50 puntos utilizados
        // y retirarse los 35 ganados.
        assertEquals(
                100,
                cliente.getPointsAccount().available(LocalDate.now())
        );

        assertEquals(
                ReservationStatus.CANCELLED,
                reserva.getStatus()
        );
    }
}