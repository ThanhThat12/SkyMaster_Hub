package com.example.controller;

import com.example.entity.RealtimeFlightEntity;
import com.example.service.RealtimeFlightService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/realtime-flights")
public class RealtimeFlightController {

    private final RealtimeFlightService realtimeFlightService;

    public RealtimeFlightController(RealtimeFlightService realtimeFlightService) {
        this.realtimeFlightService = realtimeFlightService;
    }

    @GetMapping
    public String realtimeFlightsPage(Model model) {
        // Always load cache info
        Map<String, Object> cacheInfo = realtimeFlightService.getCacheInfo();
        model.addAttribute("cacheInfo", cacheInfo);
        
        if(!model.containsAttribute("flights"))
        {
            model.addAttribute("flights", new ArrayList<RealtimeFlightEntity>());
        }
        if(!model.containsAttribute("searchPerformed"))
        {
            model.addAttribute("searchPerformed", false);
        }
       
        return "realtime-flights";
    }

    @PostMapping("/fetch")
    public String fetchRealtimeFlights(
            @RequestParam String depIata,
            RedirectAttributes redirectAttributes) {
        try {
            List<RealtimeFlightEntity> flights = realtimeFlightService.getRealtimeFlights(depIata);
            
            if (flights.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "No active flights found departing from " + depIata);
            } else {
                redirectAttributes.addFlashAttribute("successMessage", 
                    "Found " + flights.size() + " active flights from " + depIata);
            }
            
            redirectAttributes.addFlashAttribute("flights", flights);
            redirectAttributes.addFlashAttribute("searchPerformed", true);
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error: " + e.getMessage());
        }
        
        return "redirect:/realtime-flights";
    }

    @PostMapping("/clear-cache")
    public String clearCache(RedirectAttributes redirectAttributes) {
        realtimeFlightService.clearAllCache();
        redirectAttributes.addFlashAttribute("successMessage", "Cache cleared successfully!");
        return "redirect:/realtime-flights";
    }
}
