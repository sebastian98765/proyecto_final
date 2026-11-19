package com.hoteluq;

import com.hoteluq.controller.HotelController;
import com.hoteluq.model.AdditionalService;
import com.hoteluq.model.Reservation;
import com.hoteluq.model.enums.ReservationStatus;
import com.hoteluq.service.AdditionalServiceCatalog;
import com.hoteluq.service.ReportService;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SixthAdvanceTest {
    @Test void catalogRejectsUnavailableAndRepeatedServices() {
        AdditionalServiceCatalog catalog = new AdditionalServiceCatalog();
        assertEquals(2, catalog.select(List.of("Desayuno", "Spa")).size());
        assertThrows(IllegalArgumentException.class, () -> catalog.select(List.of("Spa", "spa")));
        catalog.save("Spa", 90_000, false);
        assertThrows(IllegalArgumentException.class, () -> catalog.select(List.of("Spa")));
    }

    @Test void reportsRespectConfirmedAndCancelledReservations() {
        HotelController controller = new HotelController();
        controller.login("cliente@hoteluq.com", "cliente123".toCharArray());
        LocalDate from = LocalDate.now().plusDays(2);
        Reservation reservation = controller.createDemoReservation("101", from, from.plusDays(2), List.of("Spa"));
        assertEquals(1, reservation.getServices().size());
        assertEquals("Spa", reservation.getServices().getFirst().name());
        ReportService report = new ReportService();
        String before = report.summary(controller.reservations(), from, from);
        assertTrue(before.contains("Confirmadas: 1"));
        String occupancy = report.occupancy(controller.reservations(), controller.rooms(), from, from.plusDays(1));
        assertTrue(occupancy.contains("Noches ocupadas: 2"));
        controller.reservationService().cancel(reservation, controller.wallet(),
                new com.hoteluq.pattern.behavioral.strategy.HotelCancellationPolicy(), true);
        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
        assertTrue(report.summary(controller.reservations(), from, from).contains("Ingresos de reservas confirmadas: $0.00"));
    }

    @Test void adminCanManageCatalogAndGenerateReports() {
        HotelController controller = new HotelController();
        assertThrows(SecurityException.class, () -> controller.reservationReport(LocalDate.now(), LocalDate.now()));
        controller.login("admin@hoteluq.com", "admin123".toCharArray());
        AdditionalService saved = controller.saveAdditionalService("Lavandería", 15_000, true);
        assertEquals("Lavandería", saved.name());
        assertTrue(controller.reservationReport(LocalDate.now(), LocalDate.now()).contains("HOTELUQ"));
    }
}
