package com.platform.SkyMaster_Hub.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.platform.SkyMaster_Hub.entity.FlightSchedules;

public interface FlightSchedulesRepository extends JpaRepository<FlightSchedules, Long> {
    
}
