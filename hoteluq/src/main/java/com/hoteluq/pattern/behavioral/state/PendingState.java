package com.hoteluq.pattern.behavioral.state;
import com.hoteluq.model.Reservation;import com.hoteluq.model.enums.ReservationStatus;
public class PendingState implements ReservationState {public ReservationStatus status(){return ReservationStatus.PENDING;}public void confirm(Reservation r){r.setState(new ConfirmedState());}public void cancel(Reservation r){r.setState(new CancelledState());}}
