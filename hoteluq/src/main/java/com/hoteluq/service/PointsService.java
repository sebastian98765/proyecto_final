package com.hoteluq.service;
import com.hoteluq.model.PointsAccount;import java.time.LocalDate;
public class PointsService {public int pointsForPayment(double paid){return (int)(paid/100000)*100;}public void earn(PointsAccount account,double paid,LocalDate date){account.earn(pointsForPayment(paid),date);}public void redeem(PointsAccount account,int points,LocalDate date){account.redeem(points,date);}}
