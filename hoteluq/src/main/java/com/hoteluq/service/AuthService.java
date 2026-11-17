package com.hoteluq.service;

import com.hoteluq.exception.BusinessException;
import com.hoteluq.model.User;
import com.hoteluq.model.enums.Role;
import com.hoteluq.repository.UserRepository;
import com.hoteluq.security.PasswordHasher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Registro y autenticación académica. Las contraseñas no se guardan en texto plano. */
public class AuthService {
    private final UserRepository users;
    private final PasswordHasher passwordHasher;
    private final Map<String, String> passwordHashes = new ConcurrentHashMap<>();

    public AuthService(UserRepository users, PasswordHasher passwordHasher) {
        this.users = users;
        this.passwordHasher = passwordHasher;
    }

    public User registerClient(String name, String email, char[] password) {
        return registerUser(name, email, password, Role.CLIENT);
    }

    public User registerUser(String name, String email, char[] password, Role role) {
        validateRegistration(name, email, password);
        if (role == null) {
            throw new BusinessException("Debe definir el rol del usuario.");
        }
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        if (users.findByEmail(normalizedEmail).isPresent()) {
            throw new BusinessException("Ya existe una cuenta registrada con ese correo.");
        }
        User user = new User(name.trim(), normalizedEmail, role);
        users.save(user);
        passwordHashes.put(normalizedEmail, passwordHasher.hash(password));
        return user;
    }

    public User authenticate(String email, char[] password) {
        if (email == null || email.isBlank() || password == null) {
            throw new BusinessException("Debe ingresar correo y contraseña.");
        }
        String normalizedEmail = email.trim().toLowerCase(java.util.Locale.ROOT);
        User user = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BusinessException("Credenciales incorrectas."));
        String hash = passwordHashes.get(normalizedEmail);
        if (hash == null || !passwordHasher.matches(password, hash)) {
            throw new BusinessException("Credenciales incorrectas.");
        }
        return user;
    }

    public boolean isAdministrator(User user) {
        return user != null && user.getRole() == Role.ADMIN;
    }

    public void requireAdministrator(User user) {
        if (!isAdministrator(user)) {
            throw new BusinessException("No tiene permisos para realizar esta operación.");
        }
    }

    private void validateRegistration(String name, String email, char[] password) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("El nombre es obligatorio.");
        }
        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new BusinessException("Ingrese un correo electrónico válido.");
        }
        if (password == null || password.length < 8) {
            throw new BusinessException("La contraseña debe tener al menos 8 caracteres.");
        }
    }
}
