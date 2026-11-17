package com.hoteluq.pattern.creational.room;

import com.hoteluq.model.Room;

/** Creator del patrón Factory Method: define el método que crea una habitación. */
public abstract class RoomCreator {
    public abstract Room createRoom(String number);
}
