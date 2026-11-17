package com.hoteluq.model;
import com.hoteluq.model.enums.RoomType;
public class Room { private final String number; private final RoomType type; private final int capacity; private final double pricePerNight; private boolean active=true;
 public Room(String number,RoomType type,int capacity,double pricePerNight){if(number==null||number.isBlank()||type==null||capacity<1||pricePerNight<=0)throw new IllegalArgumentException("Habitación inválida");this.number=number;this.type=type;this.capacity=capacity;this.pricePerNight=pricePerNight;}
 public String getNumber(){return number;} public RoomType getType(){return type;} public int getCapacity(){return capacity;} public double getPricePerNight(){return pricePerNight;} public boolean isActive(){return active;} public void setActive(boolean active){this.active=active;}
}
