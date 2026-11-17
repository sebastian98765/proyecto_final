package com.hoteluq.model;
import java.util.*;
public class Wallet { public record Movement(String type,double amount,String detail,java.time.LocalDateTime date){} private double balance; private boolean active=true; private final List<Movement> movements=new ArrayList<>();
 public synchronized double getBalance(){return balance;} public boolean isActive(){return active;} public void setActive(boolean a){active=a;} public synchronized List<Movement> getMovements(){return List.copyOf(movements);} public synchronized void topUp(double amount,String detail){if(amount<=0)throw new IllegalArgumentException("La recarga debe ser positiva");balance+=amount;movements.add(new Movement("RECARGA",amount,detail,java.time.LocalDateTime.now()));}
 public synchronized void charge(double amount,String detail){if(!active)throw new IllegalStateException("Tarjeta inactiva");if(amount<=0||balance<amount)throw new IllegalStateException("Saldo insuficiente o valor inválido");balance-=amount;movements.add(new Movement("PAGO",-amount,detail,java.time.LocalDateTime.now()));}
 public synchronized void refund(double amount,String detail){if(amount<=0) return;balance+=amount;movements.add(new Movement("REEMBOLSO",amount,detail,java.time.LocalDateTime.now()));}
}
