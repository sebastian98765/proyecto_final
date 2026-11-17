package com.hoteluq.repository;

import com.hoteluq.model.Room;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RoomRepository implements Repository<Room, String> {
    private final Map<String, Room> data = new LinkedHashMap<>();

    @Override
    public void save(Room room) {
        data.put(room.getNumber(), room);
    }

    @Override
    public Optional<Room> findById(String id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Room> findAll() {
        return List.copyOf(data.values());
    }

    public boolean existsById(String id) {
        return data.containsKey(id);
    }

    public boolean deleteById(String id) {
        return data.remove(id) != null;
    }
}
