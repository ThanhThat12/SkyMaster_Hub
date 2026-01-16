package com.example.controller;

import com.example.entity.RealtimeFlightEntity;
import com.example.service.RealtimeFlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/realtime-flights")
public class RealtimeFlightRestController {

    private final RealtimeFlightService realtimeFlightService;

    public RealtimeFlightRestController(RealtimeFlightService realtimeFlightService) {
        this.realtimeFlightService = realtimeFlightService;
    }

    /**
     * ✅ Fetch realtime flights with response time tracking
     */
    @GetMapping
    public ResponseEntity<?> getRealtimeFlights(
            @RequestParam(name = "dep_iata") String depIata) {
        
        try {
            System.out.println("✈️  Request: Realtime flights from " + depIata);
            
            long startTime = System.currentTimeMillis();
            
            List<RealtimeFlightEntity> flights = realtimeFlightService.getRealtimeFlights(depIata);
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("count", flights.size());
            response.put("responseTime", duration + "ms");
            response.put("data", flights);
            response.put("depIata", depIata.toUpperCase());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * ✅ Clear all cache
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<?> clearCache() {
        realtimeFlightService.clearAllCache();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Realtime flight cache cleared");
        
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Invalidate specific cache key
     */
    @PostMapping("/invalidate-cache")
    public ResponseEntity<?> invalidateCacheKey(@RequestParam String depIata) {
        realtimeFlightService.invalidateCacheKey(depIata);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cache invalidated for: " + depIata);
        
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Get cache statistics and info
     */
    @GetMapping("/cache-info")
    public ResponseEntity<?> getCacheInfo() {
        Map<String, Object> cacheInfo = realtimeFlightService.getCacheInfo();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cache", cacheInfo);
        
        return ResponseEntity.ok(response);
    }

    /**
     * ✅ Print cache stats to console
     */
    @PostMapping("/print-stats")
    public ResponseEntity<?> printCacheStats() {
        realtimeFlightService.printCacheStats();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cache stats printed to console");
        
        return ResponseEntity.ok(response);
    }
}
