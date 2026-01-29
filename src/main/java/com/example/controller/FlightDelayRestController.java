package com.example.controller;

import com.example.dto.*;
import com.example.entity.DelayEntity;
import com.example.service.FlightDelayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Flight Delays
 * Returns JSON responses for frontend consumption
 */
@RestController
@RequestMapping("/api/delays")
@CrossOrigin(origins = "*")  // Allow frontend from different domains
public class FlightDelayRestController {
    
    private final FlightDelayService flightDelayService;
    
    public FlightDelayRestController(FlightDelayService flightDelayService) {
        this.flightDelayService = flightDelayService;
    }
    
    /**
     * GET /api/delays
     * Get basic info about delays API
     */
    @GetMapping
    public ResponseEntity<?> getDelaysInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("totalStored", flightDelayService.countStoredDelays());
        info.put("cacheInfo", flightDelayService.getCacheInfo());
        info.put("availableEndpoints", List.of(
            "POST /api/delays/fetch - Fetch by airport",
            "POST /api/delays/fetch-by-airline - Fetch by airline",
            "GET /api/delays/search - Search delays",
            "GET /api/delays/stored - Get all stored delays",
            "GET /api/delays/cache-info - Get cache statistics"
        ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Flight Delays API");
        response.put("data", info);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * POST /api/delays/fetch
     * Fetch delayed flights by airport (departures or arrivals)
     * Supports both @RequestParam and @RequestBody
     */
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchDelays(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String iataCode,
            @RequestParam(required = false, defaultValue = "30") Integer minDelay,
            @RequestBody(required = false) FetchDelayRequest requestBody) {
        
        try {
            // Support both query params and request body
            String finalType = type != null ? type : (requestBody != null ? requestBody.getType() : null);
            String finalIataCode = iataCode != null ? iataCode : (requestBody != null ? requestBody.getIataCode() : null);
            int finalMinDelay = minDelay != null ? minDelay : (requestBody != null ? requestBody.getMinDelay() : 30);
            
            // Validate
            if (finalType == null || finalType.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Type is required (departures or arrivals)"
                ));
            }
            
            if (finalIataCode == null || finalIataCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "IATA code is required"
                ));
            }
            
            long startTime = System.currentTimeMillis();
            
            List<DelayEntity> flights = flightDelayService.getDelayedFlights(
                finalType, finalIataCode.toUpperCase(), finalMinDelay);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Found %d delayed flights for %s %s", 
                flights.size(), finalType, finalIataCode.toUpperCase()));
            response.put("count", flights.size());
            response.put("responseTime", duration + "ms");
            response.put("data", flights);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * POST /api/delays/fetch-by-airline
     * Fetch delayed flights by airline IATA code
     */
    @PostMapping("/fetch-by-airline")
    public ResponseEntity<?> fetchDelaysByAirline(
            @RequestParam(required = false) String airlineIata,
            @RequestParam(required = false, defaultValue = "30") Integer minDelay,
            @RequestBody(required = false) FetchByAirlineRequest requestBody) {
        
        try {
            // Support both query params and request body
            String finalAirlineIata = airlineIata != null ? airlineIata : 
                (requestBody != null ? requestBody.getAirlineIata() : null);
            int finalMinDelay = minDelay != null ? minDelay : 
                (requestBody != null ? requestBody.getMinDelay() : 30);
            
            // Validate
            if (finalAirlineIata == null || finalAirlineIata.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Airline IATA code is required"
                ));
            }
            
            long startTime = System.currentTimeMillis();
            
            List<DelayEntity> flights = flightDelayService.getDelaysByAirline(
                finalAirlineIata.toUpperCase(), finalMinDelay);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Found %d delayed flights for airline %s", 
                flights.size(), finalAirlineIata.toUpperCase()));
            response.put("airlineIata", finalAirlineIata.toUpperCase());
            response.put("minDelay", finalMinDelay);
            response.put("count", flights.size());
            response.put("responseTime", duration + "ms");
            response.put("data", flights);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * GET /api/delays/stored
     * Get all stored delays from database
     */
    @GetMapping("/stored")
    public ResponseEntity<?> getStoredDelays() {
        try {
            List<DelayEntity> delays = flightDelayService.getAllStoredDelays();
            long totalCount = flightDelayService.countStoredDelays();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Retrieved " + delays.size() + " stored delays");
            response.put("totalCount", totalCount);
            response.put("count", delays.size());
            response.put("data", delays);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * GET /api/delays/count
     * Get count of stored delays
     */
    @GetMapping("/count")
    public ResponseEntity<?> getStoredCount() {
        try {
            long count = flightDelayService.countStoredDelays();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalDelays", count);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * DELETE /api/delays/cache
     * Clear all cache entries
     */
    @DeleteMapping("/cache")
    public ResponseEntity<?> clearCache() {
        try {
            flightDelayService.clearAllCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache cleared successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * POST /api/delays/clear-cache (backward compatibility)
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<?> clearCachePost() {
        return clearCache();
    }
    
    /**
     * GET /api/delays/cache-info
     * Get cache statistics and information
     */
    @GetMapping("/cache-info")
    public ResponseEntity<?> getCacheInfo() {
        try {
            Map<String, Object> cacheInfo = flightDelayService.getCacheInfo();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache statistics");
            response.put("cache", cacheInfo);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * GET /api/delays/search
     * Search delays in database by various criteria
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchDelays(
            @RequestParam(required = false) String iataCode,
            @RequestParam(required = false) String airline,
            @RequestParam(required = false) String depIata) {
        
        try {
            List<DelayEntity> delays = flightDelayService.searchDelayedFlights(
                iataCode != null ? iataCode.trim() : null,
                airline != null ? airline.trim() : null,
                depIata != null ? depIata.trim() : null
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Found " + delays.size() + " matching flights");
            response.put("count", delays.size());
            response.put("data", delays);
            response.put("searchParams", Map.of(
                "iataCode", iataCode != null ? iataCode : "N/A",
                "airline", airline != null ? airline : "N/A",
                "depIata", depIata != null ? depIata : "N/A"
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
