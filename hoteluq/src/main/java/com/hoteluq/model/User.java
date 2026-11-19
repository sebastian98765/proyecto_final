package com.hoteluq.model;

import com.hoteluq.model.enums.Role;
import java.util.UUID;

public class User {

 private final String id = UUID.randomUUID().toString();
 private final String name;
 private final String email;
 private final Role role;

 // Cada usuario tiene su propia tarjeta y cuenta de puntos
 private final Wallet wallet = new Wallet();
 private final PointsAccount pointsAccount = new PointsAccount();

 public User(String name, String email, Role role) {
  if (name == null || name.isBlank()
          || email == null || email.isBlank()
          || role == null) {
   throw new IllegalArgumentException("Datos de usuario inválidos");
  }

  this.name = name;
  this.email = email;
  this.role = role;
 }

 public String getId() {
  return id;
 }

 public String getName() {
  return name;
 }

 public String getEmail() {
  return email;
 }

 public Role getRole() {
  return role;
 }

 public Wallet getWallet() {
  return wallet;
 }

 public PointsAccount getPointsAccount() {
  return pointsAccount;
 }
}