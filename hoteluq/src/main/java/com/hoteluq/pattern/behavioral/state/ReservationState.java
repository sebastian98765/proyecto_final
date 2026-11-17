package com.hoteluq.pattern.behavioral.state;
import com.hoteluq.model.Reservation;import com.hoteluq.model.enums.ReservationStatus;
public interface ReservationState {ReservationStatus status();void confirm(Reservation r);void cancel(Reservation r);}
