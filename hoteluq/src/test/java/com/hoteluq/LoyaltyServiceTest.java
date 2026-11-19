package com.hoteluq;

import com.hoteluq.model.PointsAccount;
import com.hoteluq.service.LoyaltyService;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class LoyaltyServiceTest {

    private final LoyaltyService loyalty = new LoyaltyService();

    @Test
    void calculaPuntosCorrectamente() {
        assertEquals(10, loyalty.calcularPuntos(100_000));
        assertEquals(25, loyalty.calcularPuntos(250_000));
        assertEquals(50, loyalty.calcularPuntos(500_000));
    }

    @Test
    void noOtorgaPuntosPorMontosPequenos() {
        assertEquals(0, loyalty.calcularPuntos(9_999));
    }

    @Test
    void rechazaMontosInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> loyalty.calcularPuntos(-10_000));
        assertThrows(IllegalArgumentException.class,
                () -> loyalty.calcularPuntos(Double.NaN));
    }

    @Test
    void acumulaPuntos() {
        PointsAccount cuenta = new PointsAccount();
        LocalDate hoy = LocalDate.now();

        cuenta.earn(50, hoy);

        assertEquals(50, cuenta.available(hoy));
    }

    @Test
    void permiteRedimirPuntos() {
        PointsAccount cuenta = new PointsAccount();
        LocalDate hoy = LocalDate.now();

        cuenta.earn(50, hoy);
        cuenta.redeem(20, hoy);

        assertEquals(30, cuenta.available(hoy));
    }

    @Test
    void noPermiteRedimirMasPuntosDeLosDisponibles() {
        PointsAccount cuenta = new PointsAccount();
        LocalDate hoy = LocalDate.now();

        cuenta.earn(10, hoy);

        assertThrows(IllegalStateException.class,
                () -> cuenta.redeem(20, hoy));
    }

    @Test
    void losPuntosVencenDespuesDeUnAno() {
        PointsAccount cuenta = new PointsAccount();
        LocalDate fecha = LocalDate.of(2025, 1, 1);

        cuenta.earn(100, fecha);

        assertEquals(100, cuenta.available(fecha));
        assertEquals(0, cuenta.available(fecha.plusYears(1)));
    }

    @Test
    void calculaDescuentoCorrectamente() {
        assertEquals(10_000, loyalty.calcularDescuento(10));
        assertEquals(25_000, loyalty.calcularDescuento(25));
    }

    @Test
    void generaDeudaCuandoCancelaPuntosYaGastados() {
        PointsAccount cuenta = new PointsAccount();
        LocalDate hoy = LocalDate.now();

        cuenta.earn(50, hoy);
        cuenta.redeem(30, hoy);
        cuenta.revoke(50, hoy);

        assertEquals(-30, cuenta.balance(hoy));
        assertEquals(30, cuenta.getDeudaPuntos());
        assertEquals(0, cuenta.available(hoy));
    }

    @Test
    void nuevasGananciasCompensanLaDeuda() {
        PointsAccount cuenta = new PointsAccount();
        LocalDate hoy = LocalDate.now();

        cuenta.earn(50, hoy);
        cuenta.redeem(30, hoy);
        cuenta.revoke(50, hoy);

        cuenta.earn(40, hoy);

        assertEquals(10, cuenta.balance(hoy));
        assertEquals(0, cuenta.getDeudaPuntos());
        assertEquals(10, cuenta.available(hoy));
    }

    @Test
    void noPermiteRedimirCuandoTieneDeuda() {
        PointsAccount cuenta = new PointsAccount();
        LocalDate hoy = LocalDate.now();

        cuenta.revoke(20, hoy);

        assertThrows(
                IllegalStateException.class,
                () -> cuenta.redeem(1, hoy)
        );
    }

    @Test
    void losPuntosVencidosNoCubrenDeudas() {
        PointsAccount cuenta = new PointsAccount();

        LocalDate fechaAnterior = LocalDate.of(2024, 1, 1);
        LocalDate hoy = LocalDate.of(2026, 1, 1);

        cuenta.earn(50, fechaAnterior);
        cuenta.revoke(20, hoy);

        assertEquals(-20, cuenta.balance(hoy));
        assertEquals(0, cuenta.available(hoy));
    }

    @Test
    void devuelveTodosLosPuntosConReembolsoCompleto() {
        assertEquals(
                50,
                loyalty.calcularPuntosADevolver(50, 350_000, 350_000)
        );
    }

    @Test
    void devuelveLaMitadDePuntosConReembolsoDelCincuentaPorCiento() {
        assertEquals(
                25,
                loyalty.calcularPuntosADevolver(50, 350_000, 175_000)
        );
    }

    @Test
    void noDevuelvePuntosSinReembolso() {
        assertEquals(
                0,
                loyalty.calcularPuntosADevolver(50, 350_000, 0)
        );
    }

    @Test
    void rechazaReembolsosInvalidos() {
        assertThrows(
                IllegalArgumentException.class,
                () -> loyalty.calcularPuntosADevolver(
                        50, 350_000, 400_000
                )
        );
    }
}