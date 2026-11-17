package com.hoteluq.pattern.behavioral.Observer;
import com.hoteluq.model.Reservation;
public class EmailNotificationObserver implements ReservationObserver {public void onReservationChanged(Reservation r,String event){System.out.println("[NOTIFICACIÓN] "+event+" para "+r.getCustomer().getEmail()+". Reserva "+r.getId());}}
