package com.hoteluq;

import com.hoteluq.exception.BusinessException;
import com.hoteluq.model.AdditionalService;
import com.hoteluq.model.Reservation;
import com.hoteluq.model.Room;
import com.hoteluq.model.User;
import com.hoteluq.model.enums.ReservationStatus;
import com.hoteluq.model.enums.Role;
import com.hoteluq.model.enums.RoomType;
import com.hoteluq.pattern.behavioral.strategy.ThreeNightsPromotion;
import com.hoteluq.pattern.creational.ReservationBuilder;
import com.hoteluq.pattern.structural.PaymentGateway;
import com.hoteluq.pattern.structural.ReservationFacade;
import com.hoteluq.repository.ReservationRepository;
import com.hoteluq.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ReservationFlowTest {
    private User customer;
    private Room room;
    private ReservationRepository repository;
    private ReservationService service;

    @BeforeEach
    void setUp() {
        customer = new User("Ana Pérez", "ana@example.com", Role.CLIENT);
        room = new Room("701", RoomType.STANDARD, 2, 100_000);
        repository = new ReservationRepository();
        service = new ReservationService(repository);
    }

    @Test
    void builderCalculatesNightsAndAdditionalServices() {
        Reservation reservation = new ReservationBuilder()
                .customer(customer)
                .room(room)
                .dates(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 13))
                .addService(new AdditionalService("Desayuno", 25_000, true))
                .build();

        assertEquals(3, reservation.nights());
        assertEquals(325_000, reservation.getTotal(), 0.001);
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
    }

    @Test
    void facadeUsesBuilderAndAppliesThreeNightPromotion() {
        ReservationFacade facade = new ReservationFacade(service);
        AtomicBoolean paymentCalled = new AtomicBoolean();
        PaymentGateway payment = (amount, reference) -> {
            paymentCalled.set(true);
            assertEquals(292_500, amount, 0.001);
            return true;
        };

        Reservation reservation = facade.book(customer, room,
                LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 13),
                List.of(new AdditionalService("Desayuno", 25_000, true)),
                payment, new ThreeNightsPromotion());

        assertTrue(paymentCalled.get());
        assertEquals(292_500, reservation.getTotal(), 0.001);
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals(1, service.all().size());
    }

    @Test
    void rejectsOverlappingReservationBeforeCharging() {
        Reservation first = new ReservationBuilder().customer(customer).room(room)
                .dates(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 13)).build();
        service.confirmReservation(first, (amount, reference) -> true);

        AtomicBoolean paymentCalled = new AtomicBoolean();
        Reservation second = new ReservationBuilder().customer(customer).room(room)
                .dates(LocalDate.of(2026, 12, 12), LocalDate.of(2026, 12, 15)).build();

        assertThrows(BusinessException.class, () -> service.confirmReservation(second,
                (amount, reference) -> {
                    paymentCalled.set(true);
                    return true;
                }));
        assertFalse(paymentCalled.get());
        assertEquals(1, service.all().size());
    }

    @Test
    void allowsBackToBackReservations() {
        Reservation first = new ReservationBuilder().customer(customer).room(room)
                .dates(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 13)).build();
        service.confirmReservation(first, (amount, reference) -> true);

        Reservation next = new ReservationBuilder().customer(customer).room(room)
                .dates(LocalDate.of(2026, 12, 13), LocalDate.of(2026, 12, 15)).build();
        assertDoesNotThrow(() -> service.confirmReservation(next, (amount, reference) -> true));
        assertEquals(2, service.all().size());
    }

    @Test
    void declinedPaymentDoesNotConfirmOrPersistReservation() {
        Reservation reservation = new ReservationBuilder().customer(customer).room(room)
                .dates(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12)).build();

        assertThrows(BusinessException.class,
                () -> service.confirmReservation(reservation, (amount, reference) -> false));
        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertTrue(service.all().isEmpty());
    }

    @Test
    void rejectsInactiveRoom() {
        room.setActive(false);
        assertFalse(service.isAvailable(room, LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12)));
    }
}
