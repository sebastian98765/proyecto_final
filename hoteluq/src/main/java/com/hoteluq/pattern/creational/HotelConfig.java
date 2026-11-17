package com.hoteluq.pattern.creational;
public final class HotelConfig {private static final HotelConfig INSTANCE=new HotelConfig();private HotelConfig(){}public static HotelConfig getInstance(){return INSTANCE;}public String hotelName(){return "HotelUQ";}public String currency(){return "COP";}}
