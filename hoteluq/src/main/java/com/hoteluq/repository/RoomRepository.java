package com.hoteluq.repository;
import com.hoteluq.model.Room;import java.util.*;
public class RoomRepository implements Repository<Room,String>{private final Map<String,Room> data=new LinkedHashMap<>();public void save(Room r){data.put(r.getNumber(),r);}public Optional<Room> findById(String id){return Optional.ofNullable(data.get(id));}public List<Room> findAll(){return List.copyOf(data.values());}}
