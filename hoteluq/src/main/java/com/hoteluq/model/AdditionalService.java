package com.hoteluq.model;
public record AdditionalService(String name,double price,boolean available){public AdditionalService{if(name==null||name.isBlank()||price<0)throw new IllegalArgumentException("Servicio inválido");}}
