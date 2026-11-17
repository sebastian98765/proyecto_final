package com.hoteluq.pattern.creational;

import com.hoteluq.exception.BusinessException;
import com.hoteluq.model.Room;
import com.hoteluq.model.enums.RoomType;
import com.hoteluq.pattern.creational.room.FamilyRoomCreator;
import com.hoteluq.pattern.creational.room.RoomCreator;
import com.hoteluq.pattern.creational.room.StandardRoomCreator;
import com.hoteluq.pattern.creational.room.SuiteRoomCreator;

import java.util.EnumMap;
import java.util.Map;

/**
 * Punto de entrada compatible con el código existente. La creación concreta
 * se delega a creadores especializados, aplicando Factory Method.
 */
public class RoomFactory {
    private final Map<RoomType, RoomCreator> creators = new EnumMap<>(RoomType.class);

    public RoomFactory() {
        creators.put(RoomType.STANDARD, new StandardRoomCreator());
        creators.put(RoomType.FAMILY, new FamilyRoomCreator());
        creators.put(RoomType.SUITE, new SuiteRoomCreator());
    }

    public Room create(RoomType type, String number) {
        if (type == null) {
            throw new BusinessException("Debe seleccionar un tipo de habitación.");
        }
        RoomCreator creator = creators.get(type);
        if (creator == null) {
            throw new BusinessException("No existe una fábrica para el tipo de habitación: " + type);
        }
        return creator.createRoom(number);
    }
}
