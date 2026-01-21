package com.example.controller;

import com.example.entity.DelayEntity;
import com.example.service.FlightDelayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/delays")
public class FlightDelayRestController {
    
    private final FlightDelayService flightDelayService;
    
    public FlightDelayRestController(FlightDelayService flightDelayService) {
        this.flightDelayService = flightDelayService;
    }
    
  
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchDelays(
            @RequestParam String type,
            @RequestParam String iataCode,
            @RequestParam(defaultValue = "30") int minDelay) {
        
        try {
            long startTime = System.currentTimeMillis();
            
            List<DelayEntity> flights = flightDelayService.getDelayedFlights(type, iataCode, minDelay);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", flights.size());
            response.put("responseTime", duration + "ms");
            response.put("data", flights);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * NEW: Get all stored delays from database
     */
    @GetMapping("/stored")
    public ResponseEntity<?> getStoredDelays() {
        try {
            List<DelayEntity> delays = flightDelayService.getAllStoredDelays();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", delays.size());
            response.put("data", delays);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * NEW: Get count of stored delays
     */
    @GetMapping("/stored/count")
    public ResponseEntity<?> getStoredCount() {
        try {
            long count = flightDelayService.countStoredDelays();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    
    @PostMapping("/clear-cache")
    public ResponseEntity<?> clearCache() {
        flightDelayService.clearAllCache();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cache cleared");
        
        return ResponseEntity.ok(response);
    }
    
    
    @GetMapping("/cache-info")
    public ResponseEntity<?> getCacheInfo() {
        Map<String, Object> cacheInfo = flightDelayService.getCacheInfo();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cache", cacheInfo);
        
        return ResponseEntity.ok(response);
    }
}
