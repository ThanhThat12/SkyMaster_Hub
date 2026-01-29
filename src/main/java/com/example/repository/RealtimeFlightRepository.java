package com.example.repository;

import com.example.entity.RealtimeFlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RealtimeFlightRepository extends JpaRepository<RealtimeFlightEntity, Long> {
    
    
    List<RealtimeFlightEntity> findByDepIata(String depIata);
    
    boolean existsByFlightIataAndUpdated(String flightIata, Long updated);
    
    RealtimeFlightEntity findByFlightIata(String flightIata);
    

    List<RealtimeFlightEntity> findByArrIata(String arrIata);
    List<RealtimeFlightEntity> findByAirlineIata(String airlineIata);
    List<RealtimeFlightEntity> findByStatus(String status);
}
