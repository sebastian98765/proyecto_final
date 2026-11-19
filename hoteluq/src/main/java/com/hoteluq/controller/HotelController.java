package com.hoteluq.controller;

import com.hoteluq.model.AdditionalService;
import com.hoteluq.model.Reservation;
import com.hoteluq.model.Room;
import com.hoteluq.model.User;
import com.hoteluq.model.Wallet;
import com.hoteluq.model.enums.RoomType;
import com.hoteluq.model.enums.Role;
import com.hoteluq.pattern.behavioral.Observer.EmailNotificationObserver;
import com.hoteluq.pattern.creational.RoomFactory;
import com.hoteluq.pattern.structural.ReservationFacade;
import com.hoteluq.pattern.structural.WalletPaymentAdapter;
import com.hoteluq.repository.ReservationRepository;
import com.hoteluq.repository.RoomRepository;
import com.hoteluq.repository.UserRepository;
import com.hoteluq.security.PasswordHasher;
import com.hoteluq.service.AuthService;
import com.hoteluq.service.ReservationService;
import com.hoteluq.service.RoomService;
import com.hoteluq.exception.BusinessException;
import com.hoteluq.pattern.structural.AdminOperationsProxy;
import com.hoteluq.service.LoyaltyService;
import com.hoteluq.service.AdditionalServiceCatalog;
import com.hoteluq.service.ReportService;

import java.time.LocalDate;
import java.util.List;

/** Controlador MVC: coordina los casos de uso sin trasladar reglas a la vista. */
public class HotelController {
    private final AdditionalServiceCatalog serviceCatalog = new AdditionalServiceCatalog();
    private final ReportService reportService = new ReportService();
    private final RoomRepository rooms = new RoomRepository();
    private final ReservationRepository reservations = new ReservationRepository();
    private final UserRepository users = new UserRepository();
    private final ReservationService reservationService = new ReservationService(reservations);
    private final LoyaltyService loyaltyService = new LoyaltyService();
    private final RoomService roomService = new RoomService(rooms, new RoomFactory());
    private final AuthService authService = new AuthService(users, new PasswordHasher());
    private final User admin;
    private final User client;
    private User usuarioActual;


    public HotelController() {
        admin = authService.registerUser("Administrador", "admin@hoteluq.com", "admin123".toCharArray(), Role.ADMIN);
        client = authService.registerClient("Cliente Demo", "cliente@hoteluq.com", "cliente123".toCharArray());
        roomService.create("101", RoomType.STANDARD);
        roomService.create("201", RoomType.FAMILY);
        roomService.create("301", RoomType.SUITE);
        client.getWallet().topUp(3_000_000, "Saldo inicial demo");
        reservationService.addObserver(new EmailNotificationObserver());
    }

    public List<Room> rooms() {
        return roomService.findActive();
    }

    public Room createRoom(User actor, String number, RoomType type) {
        authService.requireAdministrator(actor);
        return roomService.create(number, type);
    }

    public Room updateRoom(User actor, String number, RoomType type, int capacity, double price) {
        authService.requireAdministrator(actor);
        return roomService.update(number, type, capacity, price);
    }

    public void deactivateRoom(User actor, String number) {
        authService.requireAdministrator(actor);
        roomService.deactivate(number);
    }

    public void activateRoom(User actor, String number) {
        authService.requireAdministrator(actor);
        roomService.activate(number);
    }

    public void deleteRoom(User actor, String number) {
        authService.requireAdministrator(actor);
        roomService.delete(number);
    }

    public User registerClient(String name, String email, char[] password) {
        return authService.registerClient(name, email, password);
    }

    public User login(String email, char[] password) {
        usuarioActual = null;
        usuarioActual = authService.authenticate(email, password);
        return usuarioActual;
    }

    public User getUsuarioActual() {
        return usuarioActual;
    }

    public void cerrarSesion() {
        usuarioActual = null;
    }
    public List<AdditionalService> additionalServices() { return serviceCatalog.all(); }

    public AdditionalService saveAdditionalService(String name, double price, boolean available) {
        requireCurrentAdmin();
        return serviceCatalog.save(name, price, available);
    }

    private void requireCurrentAdmin() {
        if (usuarioActual == null) throw new SecurityException("Debes iniciar sesión");
        authService.requireAdministrator(usuarioActual);
    }

    public String reservationReport(LocalDate from, LocalDate to) {
        requireCurrentAdmin();
        return reportService.summary(reservationService.all(), from, to);
    }

    public String occupancyReport(LocalDate from, LocalDate to) {
        requireCurrentAdmin();
        return reportService.occupancy(reservationService.all(), rooms.findAll(), from, to);
    }

    public Reservation createDemoReservation(String roomNumber, LocalDate in, LocalDate out) {
        return createDemoReservation(roomNumber, in, out, List.of("Desayuno"));
    }

    public Reservation createDemoReservation(String roomNumber, LocalDate in, LocalDate out,
                                             List<String> serviceNames) {
        Room room = roomService.getByNumber(roomNumber);
        if (!room.isActive()) throw new BusinessException("La habitación no está disponible para reservar.");
        ReservationFacade facade = new ReservationFacade(reservationService);
        return facade.book(client, room, in, out,
                serviceCatalog.select(serviceNames),
                new WalletPaymentAdapter(client.getWallet()));
    }
    /**
     * Crea una reserva de demostración utilizando puntos.
     *
     * Cada punto equivale a $1.000 de descuento.
     * El cliente debe pagar al menos $1.000 con su tarjeta.
     */
    public Reservation createDemoReservationWithPoints(String roomNumber, LocalDate in, LocalDate out,
                                                       int points, List<String> serviceNames) {
        if (usuarioActual != client) throw new SecurityException("Solo cliente demo");
        if (points <= 0) throw new BusinessException("Debes utilizar al menos un punto");
        Room room = roomService.getByNumber(roomNumber);
        if (!room.isActive()) throw new BusinessException("Habitación inactiva");
        return new ReservationFacade(reservationService).bookWithPoints(client, room, in, out,
                serviceCatalog.select(serviceNames), new WalletPaymentAdapter(client.getWallet()), points);
    }

    public Reservation createDemoReservationWithPoints(
            String roomNumber,
            LocalDate in,
            LocalDate out,
            int puntosUtilizados
    ) {
        if (usuarioActual == null) {
            throw new SecurityException("Debes iniciar sesión para reservar");
        }

        if (usuarioActual != client) {
            throw new SecurityException(
                    "Esta operación está disponible para el cliente demo"
            );
        }

        if (puntosUtilizados <= 0) {
            throw new BusinessException(
                    "Debes utilizar al menos un punto"
            );
        }

        Room room = roomService.getByNumber(roomNumber);

        if (!room.isActive()) {
            throw new BusinessException(
                    "La habitación no está disponible para reservar"
            );
        }

        ReservationFacade facade =
                new ReservationFacade(reservationService);

        return facade.bookWithPoints(
                client,
                room,
                in,
                out,
                List.of(
                        new AdditionalService(
                                "Desayuno",
                                35_000,
                                true
                        )
                ),
                new WalletPaymentAdapter(client.getWallet()),
                puntosUtilizados
        );
    }

    public List<Reservation> reservations() { return reservationService.all(); }
    public Wallet wallet() {
        return client.getWallet();
    }
    public User admin() { return admin; }
    public User client() { return client; }
    public ReservationService reservationService() { return reservationService; }

    public void recargarTarjeta(User administrador, User cliente, double monto) {
        if (usuarioActual == null) {
            throw new SecurityException("Debe iniciar sesión");
        }

        if (administrador == null || administrador != usuarioActual) {
            throw new SecurityException("Usuario administrador no autorizado");
        }

        authService.requireAdministrator(usuarioActual);

        if (cliente == null) {
            throw new IllegalArgumentException("El cliente no puede ser nulo");
        }

        AdminOperationsProxy proxy =
                new AdminOperationsProxy(usuarioActual, cliente.getWallet());

        proxy.topUp(monto);
    }

    public void bloquearTarjeta(User cliente) {
        if (usuarioActual == null) {
            throw new SecurityException("Debe iniciar sesión");
        }

        authService.requireAdministrator(usuarioActual);

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no válido");
        }

        cliente.getWallet().setActive(false);
    }

    public void activarTarjeta(User cliente) {
        if (usuarioActual == null) {
            throw new SecurityException("Debe iniciar sesión");
        }

        authService.requireAdministrator(usuarioActual);

        if (cliente == null) {
            throw new IllegalArgumentException("Cliente no válido");
        }

        cliente.getWallet().setActive(true);
    }

    public int consultarPuntosCliente() {
        return loyaltyService.consultarPuntos(client);
    }

    public double consultarDescuentoPuntos(int puntos) {
        if (puntos > consultarPuntosCliente()) {
            throw new BusinessException(
                    "No tienes suficientes puntos disponibles"
            );
        }

        return loyaltyService.calcularDescuento(puntos);
    }
}
