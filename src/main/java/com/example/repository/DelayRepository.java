package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.DelayEntity;

@Repository
public interface DelayRepository extends JpaRepository<DelayEntity, Long> {
    
    boolean existsByFlightIataAndDepTime(String flightIata, String depTime);
    
    List<DelayEntity> findByAirlineIata(String airlineIata);
    
    List<DelayEntity> findByDepIata(String depIata);
    
    List<DelayEntity> findByArrIata(String arrIata);
    
   
    @Query("SELECT COUNT(d) FROM DelayEntity d WHERE " +
           "(:queryType IS NULL OR d.queryType = :queryType) AND " +
           "(:iataCode IS NULL OR d.iataCode = :iataCode)")
    long countByQueryTypeAndIataCode(
        @Param("queryType") String queryType,
        @Param("iataCode") String iataCode
    );
    
   
    @Modifying
    @Query("DELETE FROM DelayEntity d WHERE d.iataCode = :iataCode")
    void deleteByIataCode(@Param("iataCode") String iataCode);
    
    
    List<DelayEntity> findByQueryTypeAndDepIataAndMinDelayGreaterThanEqual(
        String queryType, 
        String depIata, 
        int minDelay
    );
    
   
    List<DelayEntity> findByQueryTypeAndArrIataAndMinDelayGreaterThanEqual(
        String queryType, 
        String arrIata, 
        int minDelay
    );
    
   
    List<DelayEntity> findAllByOrderByIdDesc();
}
