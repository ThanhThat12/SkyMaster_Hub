package com.example.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
    
 @ModelAttribute
    public void addCommonAttributes(Model model) {
        model.addAttribute("storedDelaysShown", false);
    }

    @GetMapping
    public String delaysPage(Model model){
        if(!model.containsAttribute("delays"))
        {
            model.addAttribute("delays", new ArrayList<DelayEntity>());
             model.addAttribute("showResults", false);
        
        }
        
        
       
        long storedCount = flightDelayService.countStoredDelays();
        model.addAttribute("storedCount", storedCount);
        model.addAttribute("storedDelaysShown", false);
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
            redirectAttributes.addFlashAttribute("delays", flights);
            redirectAttributes.addFlashAttribute("showResults", true);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }   
        return "redirect:/delays";
    }
    
    /**
     * NEW: Fetch delayed flights by airline IATA code
     */
    @PostMapping("/fetch-by-airline")
    public String fetchDelaysByAirline(
        @RequestParam String airlineIata,
        @RequestParam(defaultValue = "30") int minDelay,
        RedirectAttributes redirectAttributes) {
        
        try {
            // Validate input
            if (airlineIata == null || airlineIata.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "❌ Please enter a valid airline IATA code");
                return "redirect:/delays";
            }
            
            // Fetch delays by airline
            List<DelayEntity> flights = flightDelayService.getDelaysByAirline(
                airlineIata.trim().toUpperCase(), 
                minDelay
            );
            
            // Success message
            String message = String.format(
                "✈️ Found %d delayed flights for airline %s (min delay: %d minutes)",
                flights.size(),
                airlineIata.toUpperCase(),
                minDelay
            );
            
            redirectAttributes.addFlashAttribute("successMessage", message);
            redirectAttributes.addFlashAttribute("delays", flights);
            redirectAttributes.addFlashAttribute("showResults", true);
            redirectAttributes.addFlashAttribute("searchType", "airline");
            redirectAttributes.addFlashAttribute("searchedAirline", airlineIata.toUpperCase());
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error fetching airline delays: " + e.getMessage());
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
        model.addAttribute("showResults", true);
        
        // Add count and cache info
        long storedCount = flightDelayService.countStoredDelays();
        model.addAttribute("storedCount", storedCount);
        model.addAttribute("cacheInfo", flightDelayService.getCacheInfo());
        
        return "delays";
    }
    
    @GetMapping("/stored")
    public String showStoredDelays(Model model) {
        List<DelayEntity> delays = flightDelayService.getAllStoredDelays();
        model.addAttribute("delays", delays);
        model.addAttribute("showResults", true);
        model.addAttribute("storedDelaysShown", true);
        
        // Add count and cache info
        long storedCount = flightDelayService.countStoredDelays();
        model.addAttribute("storedCount", storedCount);
        model.addAttribute("cacheInfo", flightDelayService.getCacheInfo());
        
        return "delays";
    }
    
}
