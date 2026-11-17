package com.hoteluq.pattern.creational;
import com.hoteluq.model.Room;import com.hoteluq.model.enums.RoomType;
public class RoomFactory {public Room create(RoomType type,String number){return switch(type){case STANDARD->new Room(number,type,2,150000);case FAMILY->new Room(number,type,5,280000);case SUITE->new Room(number,type,3,420000);};}}
