package com.hoteluq.pattern.behavioral.strategy;
import java.time.LocalDateTime;
public class HotelCancellationPolicy implements CancellationPolicy {public double refund(double paid,LocalDateTime arrival,LocalDateTime now){return paid;}}
