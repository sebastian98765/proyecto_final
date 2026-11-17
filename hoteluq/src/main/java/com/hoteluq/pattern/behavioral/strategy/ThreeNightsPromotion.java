package com.hoteluq.pattern.behavioral.strategy;
import com.hoteluq.model.Reservation;
public class ThreeNightsPromotion implements PromotionStrategy {public double apply(Reservation r){return r.nights()>=3?r.getTotal()*.10:0;}public String description(){return "10% de descuento desde 3 noches";}}
