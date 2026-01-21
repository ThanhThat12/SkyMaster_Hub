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
    
    /**
     * Count flights by query type and IATA code
     * Used to verify cache-DB sync
     */
    @Query("SELECT COUNT(d) FROM DelayEntity d WHERE " +
           "(:queryType IS NULL OR d.queryType = :queryType) AND " +
           "(:iataCode IS NULL OR d.iataCode = :iataCode)")
    long countByQueryTypeAndIataCode(
        @Param("queryType") String queryType,
        @Param("iataCode") String iataCode
    );
    
    /**
     * Delete flights by IATA code
     * Used when re-syncing cache to DB
     */
    @Modifying
    @Query("DELETE FROM DelayEntity d WHERE d.iataCode = :iataCode")
    void deleteByIataCode(@Param("iataCode") String iataCode);
    
    /**
     * Find flights for departures query (TIER 2 DB lookup)
     * Used in 3-tier caching strategy
     */
    List<DelayEntity> findByQueryTypeAndDepIataAndMinDelayGreaterThanEqual(
        String queryType, 
        String depIata, 
        int minDelay
    );
    
    /**
     * Find flights for arrivals query (TIER 2 DB lookup)
     * Used in 3-tier caching strategy
     */
    List<DelayEntity> findByQueryTypeAndArrIataAndMinDelayGreaterThanEqual(
        String queryType, 
        String arrIata, 
        int minDelay
    );
    
    /**
     * Find all stored delays ordered by ID desc (newest first)
     * Used for displaying all fetched delays
     */
    List<DelayEntity> findAllByOrderByIdDesc();
}
