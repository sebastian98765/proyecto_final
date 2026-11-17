package com.hoteluq.pattern.structural;
public abstract class ServiceDecorator implements ServiceComponent {protected final ServiceComponent wrapped;protected ServiceDecorator(ServiceComponent w){wrapped=w;}public String description(){return wrapped.description();}public double cost(){return wrapped.cost();}}
