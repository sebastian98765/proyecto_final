package com.hoteluq.pattern.behavioral.strategy;
import java.time.*;
public class StandardCancellationPolicy implements CancellationPolicy {public double refund(double paid,LocalDateTime arrival,LocalDateTime now){long hours=Duration.between(now,arrival).toHours();if(hours>72)return paid;if(hours>=24)return paid*.8;return 0;}}
