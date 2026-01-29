package com.example.controller;

import com.example.dto.*;
import com.example.entity.RealtimeFlightEntity;
import com.example.service.RealtimeFlightService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/realtime-flights")
@CrossOrigin(origins = "*")  // Allow frontend from different domains
public class RealtimeFlightRestController {

    private final RealtimeFlightService realtimeFlightService;

    public RealtimeFlightRestController(RealtimeFlightService realtimeFlightService) {
        this.realtimeFlightService = realtimeFlightService;
    }

    
    @GetMapping("/info")
    public ResponseEntity<?> getRealtimeFlightsInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("description", "Realtime Flight Tracking API");
        info.put("availableEndpoints", List.of(
            "POST /api/realtime-flights/fetch - Fetch realtime flights",
            "GET /api/realtime-flights?dep_iata=XXX - Get flights by departure",
            "GET /api/realtime-flights/cache-info - Get cache statistics"
        ));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", info);
        
        return ResponseEntity.ok(response);
    }

    
    @GetMapping
    public ResponseEntity<?> getRealtimeFlights(
            @RequestParam(name = "dep_iata", required = false) String depIata) {
        
        try {
            if (depIata == null || depIata.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "dep_iata parameter is required"
                ));
            }
            
            System.out.println("✈️  Request: Realtime flights from " + depIata);
            
            long startTime = System.currentTimeMillis();
            
            List<RealtimeFlightEntity> flights = realtimeFlightService.getRealtimeFlights(depIata);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Found " + flights.size() + " realtime flights");
            response.put("count", flights.size());
            response.put("responseTime", duration + "ms");
            response.put("data", flights);
            response.put("depIata", depIata.toUpperCase());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    
    @PostMapping("/fetch")
    public ResponseEntity<?> fetchRealtimeFlights(
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "iataCode", required = false) String iataCode,
            @RequestBody(required = false) FetchRealtimeRequest requestBody) {
        
        try {
            // Support both query params and request body
            String finalType = type != null ? type : (requestBody != null ? requestBody.getType() : null);
            String finalIataCode = iataCode != null ? iataCode : (requestBody != null ? requestBody.getIataCode() : null);
            
            // Validate
            if (finalIataCode == null || finalIataCode.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "IATA code is required"
                ));
            }
            
            // For now, only support dep type
            if (finalType == null || finalType.equalsIgnoreCase("dep")) {
                long startTime = System.currentTimeMillis();
                
                List<RealtimeFlightEntity> flights = realtimeFlightService.getRealtimeFlights(finalIataCode);
                
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Fetched " + flights.size() + " realtime flights");
                response.put("count", flights.size());
                response.put("responseTime", duration + "ms");
                response.put("data", flights);
                
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Only 'dep' type is currently supported"
                ));
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    
    @DeleteMapping("/cache")
    public ResponseEntity<?> clearCache() {
        try {
            realtimeFlightService.clearAllCache();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Realtime flight cache cleared successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
   
    @PostMapping("/clear-cache")
    public ResponseEntity<?> clearCachePost() {
        return clearCache();
    }

    
    @PostMapping("/invalidate-cache")
    public ResponseEntity<?> invalidateCacheKey(@RequestParam String depIata) {
        try {
            realtimeFlightService.invalidateCacheKey(depIata);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache invalidated for: " + depIata.toUpperCase());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    
    @GetMapping("/cache-info")
    public ResponseEntity<?> getCacheInfo() {
        try {
            Map<String, Object> cacheInfo = realtimeFlightService.getCacheInfo();
            
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

    
    @PostMapping("/print-stats")
    public ResponseEntity<?> printCacheStats() {
        try {
            realtimeFlightService.printCacheStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cache stats printed to console");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
