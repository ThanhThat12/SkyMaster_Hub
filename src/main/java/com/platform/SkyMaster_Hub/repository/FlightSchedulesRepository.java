package com.platform.SkyMaster_Hub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.platform.SkyMaster_Hub.entity.FlightSchedules;

import java.time.LocalDateTime;
import java.util.List;


public interface FlightSchedulesRepository extends JpaRepository<FlightSchedules, Long> {
    List<FlightSchedules> findByDepIata(String depIata);

    @Query("SELECT f FROM FlightSchedules f WHERE f.depIata = :depIata AND f.fetchAt > :cutoffTime")
    List<FlightSchedules> findByDepIataAndFetchedAfter(
        @Param("depIata") String depIata,
        @Param("cutoffTime") LocalDateTime cutoffTime
    );

    void deleteByDepIata(String depIata);
}
