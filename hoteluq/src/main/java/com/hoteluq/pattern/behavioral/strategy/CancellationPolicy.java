package com.hoteluq.pattern.behavioral.strategy;
import java.time.Duration;import java.time.LocalDateTime;
public interface CancellationPolicy {double refund(double paid,LocalDateTime arrival,LocalDateTime now);}
