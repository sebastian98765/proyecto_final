package com.hoteluq.pattern.creational.room;

import com.hoteluq.model.Room;
import com.hoteluq.model.enums.RoomType;

public class SuiteRoomCreator extends RoomCreator {
    @Override
    public Room createRoom(String number) {
        return new Room(number, RoomType.SUITE, 3, 420_000);
    }
}
