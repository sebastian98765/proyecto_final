package com.hoteluq.pattern.creational.room;

import com.hoteluq.model.Room;
import com.hoteluq.model.enums.RoomType;

public class StandardRoomCreator extends RoomCreator {
    @Override
    public Room createRoom(String number) {
        return new Room(number, RoomType.STANDARD, 2, 150_000);
    }
}
