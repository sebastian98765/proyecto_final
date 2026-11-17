package com.hoteluq.pattern.behavioral.state;
import com.hoteluq.model.Reservation;import com.hoteluq.model.enums.ReservationStatus;
public class CancelledState implements ReservationState {public ReservationStatus status(){return ReservationStatus.CANCELLED;}public void confirm(Reservation r){throw new IllegalStateException("Una reserva cancelada no se confirma");}public void cancel(Reservation r){throw new IllegalStateException("La reserva ya está cancelada");}}
