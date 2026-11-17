package com.hoteluq.repository;
import com.hoteluq.model.Reservation;import java.util.*;
public class ReservationRepository implements Repository<Reservation,String>{private final Map<String,Reservation> data=new LinkedHashMap<>();public void save(Reservation r){data.put(r.getId(),r);}public Optional<Reservation> findById(String id){return Optional.ofNullable(data.get(id));}public List<Reservation> findAll(){return List.copyOf(data.values());}}
