package com.example.repository;

import com.example.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<ScheduleEntity, Long> {
    
    // Tìm theo arr_iata
    List<ScheduleEntity> findByArrIata(String arrIata);
    
    // Tìm theo airline
    List<ScheduleEntity> findByAirlineIata(String airlineIata);
    
    // Tìm theo departure airport
    List<ScheduleEntity> findByDepIata(String depIata);
    
    // Check xem flight đã tồn tại chưa
    boolean existsByFlightIata(String flightIata);
}
