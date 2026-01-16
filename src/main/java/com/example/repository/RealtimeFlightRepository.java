package com.example.repository;

import com.example.entity.RealtimeFlightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RealtimeFlightRepository extends JpaRepository<RealtimeFlightEntity, Long> {
    
    // db lookup by dep_iata
    List<RealtimeFlightEntity> findByDepIata(String depIata);
    
    // check duplicate
    boolean existsByFlightIataAndUpdated(String flightIata, Long updated);
    
    // Find existing flight by flight_iata for update
    RealtimeFlightEntity findByFlightIata(String flightIata);
    
    // search methods
    List<RealtimeFlightEntity> findByArrIata(String arrIata);
    List<RealtimeFlightEntity> findByAirlineIata(String airlineIata);
    List<RealtimeFlightEntity> findByStatus(String status);
}
