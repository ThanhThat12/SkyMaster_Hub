package com.example.controller;

import com.example.entity.ScheduleEntity;
import com.example.service.FlightScheduleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class FlightScheduleController {
    
    private final FlightScheduleService flightScheduleService;
    
    public FlightScheduleController(FlightScheduleService flightScheduleService) {
        this.flightScheduleService = flightScheduleService;
    }
    
   
    @GetMapping("/")
    public String index(Model model) {
        // Load all schedules by default
        List<ScheduleEntity> schedules = flightScheduleService.searchSchedules(null, null, null);
        model.addAttribute("schedules", schedules);
        model.addAttribute("totalSchedules", schedules.size());
        return "index";
    }
    
    
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String arrIata,
                        @RequestParam(required = false) String airline,
                        @RequestParam(required = false) String depIata,
                        Model model) {
        
        List<ScheduleEntity> schedules = flightScheduleService.searchSchedules(arrIata, airline, depIata);
        
        model.addAttribute("schedules", schedules);
        model.addAttribute("totalSchedules", schedules.size());
        model.addAttribute("searchArrIata", arrIata);
        model.addAttribute("searchAirline", airline);
        model.addAttribute("searchDepIata", depIata);
        
        return "index";
    }
    
    
    @PostMapping("/fetch")
    public String fetchSchedules(@RequestParam String arrIata, Model model) {
        try {
            if (arrIata == null || arrIata.trim().isEmpty()) {
                model.addAttribute("error", "Please enter arrival airport IATA code");
                return "redirect:/";
            }
            
            if (arrIata.length() != 3) {
                model.addAttribute("error", "IATA code must be exactly 3 characters");
                return "redirect:/";
            }
            
            
            List<ScheduleEntity> schedules = flightScheduleService.fetchAndSaveSchedules(arrIata.toUpperCase());
            
            model.addAttribute("success", "Successfully fetched " + schedules.size() + " flights to " + arrIata.toUpperCase());
            model.addAttribute("schedules", schedules);
            model.addAttribute("totalSchedules", schedules.size());
            
            return "index";
            
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching schedules: " + e.getMessage());
            List<ScheduleEntity> schedules = flightScheduleService.searchSchedules(null, null, null);
            model.addAttribute("schedules", schedules);
            model.addAttribute("totalSchedules", schedules.size());
            return "index";
        }
    }
}
