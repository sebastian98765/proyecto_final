package com.hoteluq.repository;

import com.hoteluq.model.User;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UserRepository implements Repository<User, String> {
    private final Map<String, User> usersById = new LinkedHashMap<>();
    private final Map<String, User> usersByEmail = new LinkedHashMap<>();

    @Override
    public void save(User user) {
        String email = normalize(user.getEmail());
        User existing = usersByEmail.get(email);
        if (existing != null && !existing.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo.");
        }
        usersById.put(user.getId(), user);
        usersByEmail.put(email, user);
    }

    @Override
    public Optional<User> findById(String id) {
        return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public List<User> findAll() {
        return List.copyOf(usersById.values());
    }

    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();
        return Optional.ofNullable(usersByEmail.get(normalize(email)));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
