package com.platform.SkyMaster_Hub.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.platform.SkyMaster_Hub.service.CacheManagementService;

@RestController
@RequestMapping("/api/cache-management")
public class CacheManagementController {

    private final CacheManagementService cacheManagementService;

    public CacheManagementController(CacheManagementService cacheManagementService) {
        this.cacheManagementService = cacheManagementService;
    }

    // Endpoint to get all cache keys in flightSchedules cache
    @GetMapping("/flight-schedules/keys")  
    public ResponseEntity<Set<Object>> getCacheKeys(){
        return ResponseEntity.ok(cacheManagementService.getCacheKeys("flightSchedules"));
    }


    // Endpoint to get all cache flightSchedules data
    @GetMapping("/flight-schedules/data")
    public ResponseEntity<Map<Object, Object>> getAllCacheData(){
        return ResponseEntity.ok(cacheManagementService.getAllCacheData());
    }
    
}
