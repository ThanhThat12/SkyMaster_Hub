package com.platform.SkyMaster_Hub.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.platform.SkyMaster_Hub.dto.FlightScheduleDTO;
import com.platform.SkyMaster_Hub.service.FlightScheduleService;

@RestController
@RequestMapping("/api/flightschedules")
public class FlightScheduleController {
    private final FlightScheduleService flightScheduleService;

    public FlightScheduleController(FlightScheduleService flightScheduleService) {
        this.flightScheduleService = flightScheduleService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFlightSchedules(
        @RequestParam("dep_iata") String depIata){
            try{
                List<FlightScheduleDTO> schedules = flightScheduleService.getFlightSchedules(depIata);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Success");
                response.put("dep_iata", depIata);
                response.put("count", schedules.size());
                response.put("data", schedules);

                return ResponseEntity.ok(response);
            }
            catch(Exception e){
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error occurred");
                errorResponse.put("error", e.getMessage());
                return ResponseEntity.status(500).body(errorResponse);
            }
        }

    // @PostMapping("/refresh")
    // public ResponseEntity<Map<String, Object>> refreshFlightSchedules(
    //     @RequestParam("dep_iata") String depIata){
    // try{
    //     List<FlightScheduleDTO> schedules = flightScheduleService.forceRefreshFlightSchedules(depIata);

    //     Map<String, Object> response = new HashMap<>();
    //     response.put("message", "success");
    //     response.put("dep_iata", depIata);
    //     response.put("count", schedules.size());
    //     response.put("data", schedules);

    //     return ResponseEntity.ok(response);

    // }
    // catch(Exception e){
    //     Map<String, Object> errorResponse = new HashMap<>();
    //     errorResponse.put("message", "error");
    //     errorResponse.put("error", e.getMessage());
    //     errorResponse.put("dep_iata", depIata);
    
    //     return ResponseEntity.badRequest().body(errorResponse);
    // }
// }
}
