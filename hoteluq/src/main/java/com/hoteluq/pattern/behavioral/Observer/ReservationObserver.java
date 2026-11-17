package com.hoteluq.pattern.behavioral.Observer;
import com.hoteluq.model.Reservation;
public interface ReservationObserver {void onReservationChanged(Reservation reservation,String event);}
