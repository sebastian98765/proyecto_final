package com.hoteluq.service;

import com.hoteluq.exception.BusinessException;
import com.hoteluq.model.Room;
import com.hoteluq.model.enums.RoomType;
import com.hoteluq.pattern.creational.RoomFactory;
import com.hoteluq.repository.RoomRepository;

import java.util.List;

/** Casos de uso para administrar el catálogo de habitaciones. */
public class RoomService {
    private final RoomRepository repository;
    private final RoomFactory factory;

    public RoomService(RoomRepository repository, RoomFactory factory) {
        this.repository = repository;
        this.factory = factory;
    }

    public Room create(String number, RoomType type) {
        validateNumber(number);
        if (repository.existsById(number.trim())) {
            throw new BusinessException("Ya existe una habitación con el número " + number + ".");
        }
        Room room = factory.create(type, number.trim());
        repository.save(room);
        return room;
    }

    public Room update(String number, RoomType newType, int capacity, double pricePerNight) {
        Room current = getByNumber(number);
        if (newType == null) {
            throw new BusinessException("Debe seleccionar un tipo de habitación.");
        }
        if (capacity < 1) {
            throw new BusinessException("La capacidad debe ser de al menos una persona.");
        }
        if (!Double.isFinite(pricePerNight) || pricePerNight <= 0) {
            throw new BusinessException("La tarifa por noche debe ser mayor que cero.");
        }
        Room updated = new Room(current.getNumber(), newType, capacity, pricePerNight);
        updated.setActive(current.isActive());
        repository.save(updated);
        return updated;
    }

    public void deactivate(String number) {
        Room room = getByNumber(number);
        room.setActive(false);
    }

    public void activate(String number) {
        Room room = getByNumber(number);
        room.setActive(true);
    }

    /** El borrado físico solo se permite para habitaciones previamente inactivadas. */
    public void delete(String number) {
        Room room = getByNumber(number);
        if (room.isActive()) {
            throw new BusinessException("Desactive la habitación antes de eliminarla.");
        }
        repository.deleteById(room.getNumber());
    }

    public Room getByNumber(String number) {
        validateNumber(number);
        return repository.findById(number.trim())
                .orElseThrow(() -> new BusinessException("No existe la habitación " + number + "."));
    }

    public List<Room> findAll() {
        return repository.findAll();
    }

    public List<Room> findActive() {
        return repository.findAll().stream().filter(Room::isActive).toList();
    }

    private void validateNumber(String number) {
        if (number == null || number.isBlank()) {
            throw new BusinessException("El número de habitación es obligatorio.");
        }
    }
}
