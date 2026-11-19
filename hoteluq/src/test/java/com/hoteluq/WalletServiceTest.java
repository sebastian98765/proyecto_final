package com.hoteluq;

import com.hoteluq.controller.HotelController;
import com.hoteluq.model.User;
import com.hoteluq.model.enums.Role;
import com.hoteluq.pattern.structural.AdminOperationsProxy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class WalletServiceTest {

    @Test
    void administradorPuedeRecargarTarjeta() {
        User admin = new User("Administrador", "admin@test.com", Role.ADMIN);
        User cliente = new User("Cliente", "cliente@test.com", Role.CLIENT);

        AdminOperationsProxy proxy =
                new AdminOperationsProxy(admin, cliente.getWallet());

        proxy.topUp(150_000);

        assertEquals(150_000, cliente.getWallet().getBalance());
        assertEquals(1, cliente.getWallet().getMovements().size());
    }

    @Test
    void clienteNoPuedeRecargarTarjeta() {
        User cliente = new User("Cliente", "cliente@test.com", Role.CLIENT);

        AdminOperationsProxy proxy =
                new AdminOperationsProxy(cliente, cliente.getWallet());

        assertThrows(SecurityException.class,
                () -> proxy.topUp(100_000));

        assertEquals(0, cliente.getWallet().getBalance());
    }

    @Test
    void noPermiteRecargasNegativas() {
        User admin = new User("Administrador", "admin@test.com", Role.ADMIN);
        User cliente = new User("Cliente", "cliente@test.com", Role.CLIENT);

        AdminOperationsProxy proxy =
                new AdminOperationsProxy(admin, cliente.getWallet());

        assertThrows(IllegalArgumentException.class,
                () -> proxy.topUp(-50_000));

        assertEquals(0, cliente.getWallet().getBalance());
    }

    @Test
    void clientePuedePagarConTarjeta() {
        User cliente = new User("Cliente", "cliente@test.com", Role.CLIENT);

        cliente.getWallet().topUp(200_000, "Recarga inicial");
        cliente.getWallet().charge(80_000, "Pago de reserva");

        assertEquals(120_000, cliente.getWallet().getBalance());
        assertEquals(2, cliente.getWallet().getMovements().size());
        assertEquals("PAGO",
                cliente.getWallet().getMovements().get(1).type());
    }

    @Test
    void noPermitePagarSinSaldoSuficiente() {
        User cliente = new User("Cliente", "cliente@test.com", Role.CLIENT);

        cliente.getWallet().topUp(50_000, "Recarga inicial");

        assertThrows(IllegalStateException.class,
                () -> cliente.getWallet().charge(100_000, "Reserva"));

        assertEquals(50_000, cliente.getWallet().getBalance());
    }

    @Test
    void tarjetaBloqueadaNoPermitePagos() {
        User cliente = new User("Cliente", "cliente@test.com", Role.CLIENT);

        cliente.getWallet().topUp(100_000, "Recarga inicial");
        cliente.getWallet().setActive(false);

        assertThrows(IllegalStateException.class,
                () -> cliente.getWallet().charge(50_000, "Reserva"));

        assertEquals(100_000, cliente.getWallet().getBalance());
    }

    @Test
    void tarjetaReactivadaPermitePagos() {
        User cliente = new User("Cliente", "cliente@test.com", Role.CLIENT);

        cliente.getWallet().topUp(100_000, "Recarga inicial");
        cliente.getWallet().setActive(false);
        cliente.getWallet().setActive(true);

        cliente.getWallet().charge(40_000, "Reserva");

        assertEquals(60_000, cliente.getWallet().getBalance());
    }

    @Test
    void permiteConsultarMovimientos() {
        User cliente = new User("Cliente", "cliente@test.com", Role.CLIENT);

        cliente.getWallet().topUp(200_000, "Recarga inicial");
        cliente.getWallet().charge(80_000, "Pago de reserva");

        var movimientos = cliente.getWallet().getMovements();

        assertEquals(2, movimientos.size());
        assertEquals("RECARGA", movimientos.get(0).type());
        assertEquals("PAGO", movimientos.get(1).type());
        assertEquals(120_000, cliente.getWallet().getBalance());
    }

    @Test
    void clienteNoPuedeBloquearTarjetaDesdeControlador() {
        HotelController controller = new HotelController();

        controller.login(
                "cliente@hoteluq.com",
                "cliente123".toCharArray()
        );

        assertThrows(
                RuntimeException.class,
                () -> controller.bloquearTarjeta(controller.client())
        );

        assertTrue(controller.client().getWallet().isActive());
    }

    @Test
    void clienteNoPuedeRecargarDesdeControlador() {
        HotelController controller = new HotelController();

        User cliente = controller.login(
                "cliente@hoteluq.com",
                "cliente123".toCharArray()
        );

        double saldoInicial = cliente.getWallet().getBalance();

        assertThrows(
                RuntimeException.class,
                () -> controller.recargarTarjeta(
                        cliente,
                        cliente,
                        100_000
                )
        );

        assertEquals(
                saldoInicial,
                cliente.getWallet().getBalance()
        );
    }
}