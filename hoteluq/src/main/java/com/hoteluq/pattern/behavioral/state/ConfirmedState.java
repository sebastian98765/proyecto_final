package com.hoteluq.pattern.behavioral.state;
import com.hoteluq.model.Reservation;import com.hoteluq.model.enums.ReservationStatus;
public class ConfirmedState implements ReservationState {public ReservationStatus status(){return ReservationStatus.CONFIRMED;}public void confirm(Reservation r){throw new IllegalStateException("La reserva ya está confirmada");}public void cancel(Reservation r){r.setState(new CancelledState());}}
