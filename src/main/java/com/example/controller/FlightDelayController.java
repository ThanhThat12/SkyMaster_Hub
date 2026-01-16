package com.example.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.entity.DelayEntity;
import com.example.service.FlightDelayService;

@Controller
@RequestMapping("/delays")
public class FlightDelayController {
    private final FlightDelayService flightDelayService;
    public FlightDelayController(FlightDelayService flightDelayService) {
        this.flightDelayService = flightDelayService;
    }
    @GetMapping
    public String delaysPage(Model model){
        List<DelayEntity> delays = flightDelayService.searchDelayedFlights(null, null, null);
        model.addAttribute("delays", delays);
        
        // Add cache info for monitoring
        model.addAttribute("cacheInfo", flightDelayService.getCacheInfo());
        
        return "delays";
    }
    @PostMapping("/fetch")
    public String fetchDelays(
        @RequestParam String type,
        @RequestParam String iataCode,
        @RequestParam(defaultValue = "30") int minDelay,
        RedirectAttributes redirectAttributes){
        try {
            List<DelayEntity> flights = flightDelayService.getDelayedFlights(type, iataCode, minDelay);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Fetched " + flights.size() + " delayed flights for " + iataCode);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }   
        return "redirect:/delays";
    }
    @GetMapping("/search")
    public String searchDelays(
        @RequestParam(required = false) String iataCode,
        @RequestParam(required = false) String airline,
        @RequestParam(required = false) String depIata,
        Model model){
        List<DelayEntity> delays = flightDelayService.searchDelayedFlights(iataCode, airline, depIata);
        model.addAttribute("delays", delays);
        model.addAttribute("searchPerformed", true);   
        return "delays";
        }
    
}
