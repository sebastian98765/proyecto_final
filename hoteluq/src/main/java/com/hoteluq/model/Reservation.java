package com.hoteluq.model;

import com.hoteluq.model.enums.ReservationStatus;
import com.hoteluq.pattern.behavioral.state.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.time.temporal.ChronoUnit;

public class Reservation {

 private final String id = UUID.randomUUID().toString();

 private final User customer;
 private final Room room;

 private final LocalDate checkIn;
 private final LocalDate checkOut;

 private final List<AdditionalService> services;
 private final double total;

 private ReservationState state = new PendingState();

 private boolean refunded;

 // Atributos del sistema de fidelización
 private int puntosUtilizados;
 private int puntosGanados;

 public Reservation(
         User customer,
         Room room,
         LocalDate checkIn,
         LocalDate checkOut,
         List<AdditionalService> services,
         double total
 ) {
  this.customer = Objects.requireNonNull(customer);
  this.room = Objects.requireNonNull(room);

  if (checkIn == null || checkOut == null
          || !checkOut.isAfter(checkIn)) {
   throw new IllegalArgumentException(
           "La salida debe ser posterior a la entrada"
   );
  }

  this.checkIn = checkIn;
  this.checkOut = checkOut;
  this.services = new ArrayList<>(services);
  this.total = total;

  this.puntosUtilizados = 0;
  this.puntosGanados = 0;
 }

 public String getId() {
  return id;
 }

 public User getCustomer() {
  return customer;
 }

 public Room getRoom() {
  return room;
 }

 public LocalDate getCheckIn() {
  return checkIn;
 }

 public LocalDate getCheckOut() {
  return checkOut;
 }

 public List<AdditionalService> getServices() {
  return List.copyOf(services);
 }

 public double getTotal() {
  return total;
 }

 public ReservationStatus getStatus() {
  return state.status();
 }

 public void confirm() {
  state.confirm(this);
 }

 public void cancel() {
  state.cancel(this);
 }

 public void setState(ReservationState state) {
  this.state = state;
 }

 public boolean isRefunded() {
  return refunded;
 }

 public void markRefunded() {
  if (refunded) {
   throw new IllegalStateException(
           "Pago ya reembolsado"
   );
  }

  refunded = true;
 }

 public long nights() {
  return ChronoUnit.DAYS.between(checkIn, checkOut);
 }

 // =====================================
 // SISTEMA DE PUNTOS Y FIDELIZACIÓN
 // =====================================

 public int getPuntosUtilizados() {
  return puntosUtilizados;
 }

 public void setPuntosUtilizados(int puntosUtilizados) {
  if (puntosUtilizados < 0) {
   throw new IllegalArgumentException(
           "Los puntos utilizados no pueden ser negativos"
   );
  }

  this.puntosUtilizados = puntosUtilizados;
 }

 public int getPuntosGanados() {
  return puntosGanados;
 }

 public void setPuntosGanados(int puntosGanados) {
  if (puntosGanados < 0) {
   throw new IllegalArgumentException(
           "Los puntos ganados no pueden ser negativos"
   );
  }

  this.puntosGanados = puntosGanados;
 }
}