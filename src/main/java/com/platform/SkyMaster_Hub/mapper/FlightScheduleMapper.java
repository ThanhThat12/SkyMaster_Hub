package com.platform.SkyMaster_Hub.mapper;
import org.springframework.stereotype.Component;

import com.platform.SkyMaster_Hub.dto.FlightScheduleDTO;
import com.platform.SkyMaster_Hub.entity.FlightSchedules;


@Component
public class FlightScheduleMapper {
    public FlightSchedules toEntity(FlightScheduleDTO dto){
        if(dto == null){
            return null;
        }

        FlightSchedules schedule = new FlightSchedules();
        schedule.setFlightIata(dto.getFlightIata());


        return schedule;
    }

    public FlightScheduleDTO toDTO(FlightSchedules entity){
        if(entity == null){
            return null;
        }

        FlightScheduleDTO schedule = new FlightScheduleDTO();
        schedule.setFlightIata(entity.getFlightIata());
        return schedule;
    }
}
