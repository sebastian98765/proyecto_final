package com.hoteluq.pattern.structural;
public class BreakfastDecorator extends ServiceDecorator {public BreakfastDecorator(ServiceComponent w){super(w);}public String description(){return wrapped.description()+" + desayuno";}public double cost(){return wrapped.cost()+35000;}}
