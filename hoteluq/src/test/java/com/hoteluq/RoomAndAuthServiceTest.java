package com.hoteluq;

import com.hoteluq.exception.BusinessException;
import com.hoteluq.model.Room;
import com.hoteluq.model.User;
import com.hoteluq.model.enums.RoomType;
import com.hoteluq.model.enums.Role;
import com.hoteluq.pattern.creational.RoomFactory;
import com.hoteluq.repository.RoomRepository;
import com.hoteluq.repository.UserRepository;
import com.hoteluq.security.PasswordHasher;
import com.hoteluq.service.AuthService;
import com.hoteluq.service.RoomService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RoomAndAuthServiceTest {
    private RoomRepository roomRepository;
    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomRepository = new RoomRepository();
        roomService = new RoomService(roomRepository, new RoomFactory());
    }

    @Test
    void createsRoomUsingSelectedFactoryMethod() {
        Room room = roomService.create("501", RoomType.SUITE);
        assertEquals(RoomType.SUITE, room.getType());
        assertEquals(420_000, room.getPricePerNight());
    }

    @Test
    void rejectsDuplicateRoomNumber() {
        roomService.create("101", RoomType.STANDARD);
        assertThrows(BusinessException.class,
                () -> roomService.create("101", RoomType.FAMILY));
    }

    @Test
    void updatesRoomDetailsWithoutChangingItsNumber() {
        roomService.create("102", RoomType.STANDARD);
        Room updated = roomService.update("102", RoomType.FAMILY, 6, 300_000);
        assertEquals("102", updated.getNumber());
        assertEquals(RoomType.FAMILY, updated.getType());
        assertEquals(6, updated.getCapacity());
        assertEquals(300_000, updated.getPricePerNight());
    }

    @Test
    void requiresDeactivationBeforePhysicalDeletion() {
        roomService.create("103", RoomType.STANDARD);
        assertThrows(BusinessException.class, () -> roomService.delete("103"));
        roomService.deactivate("103");
        roomService.delete("103");
        assertTrue(roomRepository.findById("103").isEmpty());
    }

    @Test
    void registersAndAuthenticatesClient() {
        AuthService auth = new AuthService(new UserRepository(), new PasswordHasher());
        User user = auth.registerClient("Ana Pérez", "ANA@example.com", "ClaveSegura9".toCharArray());
        User authenticated = auth.authenticate("ana@example.com", "ClaveSegura9".toCharArray());
        assertEquals(user.getId(), authenticated.getId());
        assertEquals(Role.CLIENT, authenticated.getRole());
    }

    @Test
    void rejectsWrongPassword() {
        AuthService auth = new AuthService(new UserRepository(), new PasswordHasher());
        auth.registerClient("Ana", "ana@example.com", "ClaveSegura9".toCharArray());
        assertThrows(BusinessException.class,
                () -> auth.authenticate("ana@example.com", "incorrecta".toCharArray()));
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        AuthService auth = new AuthService(new UserRepository(), new PasswordHasher());
        auth.registerClient("Ana", "ana@example.com", "ClaveSegura9".toCharArray());
        assertThrows(BusinessException.class,
                () -> auth.registerClient("Otra Ana", "ANA@example.com", "OtraClave9".toCharArray()));
    }

    @Test
    void clientCannotExecuteAdministratorOperation() {
        AuthService auth = new AuthService(new UserRepository(), new PasswordHasher());
        User client = auth.registerClient("Ana", "ana@example.com", "ClaveSegura9".toCharArray());
        assertThrows(BusinessException.class, () -> auth.requireAdministrator(client));
    }
}
