package com.platform.SkyMaster_Hub.mapper;

import org.springframework.stereotype.Component;
import com.platform.SkyMaster_Hub.dto.FlightScheduleDTO;
import com.platform.SkyMaster_Hub.entity.FlightSchedules;
import java.time.LocalDateTime;

@Component
public class FlightScheduleMapper {
    
    public FlightSchedules toEntity(FlightScheduleDTO dto) {
        if (dto == null) {
            return null;
        }

        FlightSchedules schedule = new FlightSchedules();
        schedule.setFlightIata(dto.getFlightIata());
        schedule.setFlightIcao(dto.getFlightIcao());
        schedule.setAirlineIata(dto.getAirlineIata());
        schedule.setAirlineIcao(dto.getAirlineIcao());
        schedule.setDepIata(dto.getDepIata());
        schedule.setDepIcao(dto.getDepIcao());
        schedule.setDepTime(dto.getDepTime());
        schedule.setArrIata(dto.getArrIata());
        schedule.setArrIcao(dto.getArrIcao());
        schedule.setArrTime(dto.getArrTime());
        schedule.setDuration(dto.getDuration());
        schedule.setStatus(dto.getStatus());
        schedule.setFetchAt(LocalDateTime.now());

        return schedule;
    }

    public FlightScheduleDTO toDTO(FlightSchedules entity) {
        if (entity == null) {
            return null;
        }

        FlightScheduleDTO dto = new FlightScheduleDTO();
        dto.setFlightIata(entity.getFlightIata());
        dto.setFlightIcao(entity.getFlightIcao());
        dto.setAirlineIata(entity.getAirlineIata());
        dto.setAirlineIcao(entity.getAirlineIcao());
        dto.setDepIata(entity.getDepIata());
        dto.setDepIcao(entity.getDepIcao());
        dto.setDepTime(entity.getDepTime());
        dto.setArrIata(entity.getArrIata());
        dto.setArrIcao(entity.getArrIcao());
        dto.setArrTime(entity.getArrTime());
        dto.setDuration(entity.getDuration());
        dto.setStatus(entity.getStatus());
        
        return dto;
    }
}
