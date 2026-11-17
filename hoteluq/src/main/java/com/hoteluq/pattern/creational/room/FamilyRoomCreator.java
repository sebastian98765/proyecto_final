package com.hoteluq.pattern.creational.room;

import com.hoteluq.model.Room;
import com.hoteluq.model.enums.RoomType;

public class FamilyRoomCreator extends RoomCreator {
    @Override
    public Room createRoom(String number) {
        return new Room(number, RoomType.FAMILY, 5, 280_000);
    }
}
