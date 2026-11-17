package com.hoteluq.pattern.behavioral.strategy;
import com.hoteluq.model.Reservation;
public interface PromotionStrategy {double apply(Reservation reservation);String description();}
