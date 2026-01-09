package com.platform.SkyMaster_Hub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.platform.SkyMaster_Hub.entity.Airlines;
import com.platform.SkyMaster_Hub.entity.Airports;
import com.platform.SkyMaster_Hub.entity.Cities;
import com.platform.SkyMaster_Hub.entity.Countries;
import com.platform.SkyMaster_Hub.service.AirLabService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/airlab")
public class AirLabController {
    
    private final AirLabService airLabService;
    
    public AirLabController(AirLabService airLabService) {
        this.airLabService = airLabService;
    }
    
    @GetMapping("/fetch/airports")
    public ResponseEntity<Map<String, Object>> fetchAirports() {
        try {
            List<Airports> airports = airLabService.fetchAndSaveAirports();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved airports");
            response.put("count", airports.size());
            response.put("data", airports);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch airports from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @GetMapping("/fetch/airlines")
    public ResponseEntity<Map<String, Object>> fetchAirlines() {
        
        try {
            List<Airlines> airlines = airLabService.fetchAndSaveAirlines();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved airlines");
            response.put("count", airlines.size());
            response.put("data", airlines);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch airlines from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    
    /**
     * Get all airports from database
     * GET /api/airlab/airports
     */
    @GetMapping("/airports")
    public ResponseEntity<List<Airports>> getAllAirports() {
        
        List<Airports> airports = airLabService.getAllAirports();
        return ResponseEntity.ok(airports);
    }
    
    /**
     * Get all airlines from database
     * GET /api/airlab/airlines
     */
    @GetMapping("/airlines")
    public ResponseEntity<List<Airlines>> getAllAirlines() {
        
        List<Airlines> airlines = airLabService.getAllAirlines();
        return ResponseEntity.ok(airlines);
    }
    
    /**
     * Get airport by ICAO code
     * GET /api/airlab/airports/{icaoCode}
     */
    @GetMapping("/airports/{icaoCode}")
    public ResponseEntity<Airports> getAirportByIcaoCode(@PathVariable String icaoCode) {
        
        Airports airport = airLabService.getAirportByIcaoCode(icaoCode);
        return ResponseEntity.ok(airport);
    }
    
    /**
     * Get airline by IATA code
     * GET /api/airlab/airlines/{iataCode}
     */
    @GetMapping("/airlines/{iataCode}")
    public ResponseEntity<Airlines> getAirlineByIataCode(@PathVariable String iataCode) {
        
        Airlines airline = airLabService.getAirlineByIataCode(iataCode);
        return ResponseEntity.ok(airline);
    }
    
    
    // ============= COUNTRIES ENDPOINTS =============
    
    /**
     * Fetch and save countries from AirLab API
     * GET /api/airlab/fetch/countries
     */
    @GetMapping("/fetch/countries")
    public ResponseEntity<Map<String, Object>> fetchCountries() {
        try {
            List<Countries> countries = airLabService.fetchAndSaveCountries();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved countries");
            response.put("count", countries.size());
            response.put("data", countries);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch countries from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get all countries from database
     * GET /api/airlab/countries
     */
    @GetMapping("/countries")
    public ResponseEntity<List<Countries>> getAllCountries() {
        List<Countries> countries = airLabService.getAllCountries();
        return ResponseEntity.ok(countries);
    }
    
    /**
     * Get country by code
     * GET /api/airlab/countries/{code}
     */
    @GetMapping("/countries/{code}")
    public ResponseEntity<Countries> getCountryByCode(@PathVariable String code) {
        Countries country = airLabService.getCountryByCode(code);
        return ResponseEntity.ok(country);
    }
    
    
    // ============= CITIES ENDPOINTS =============
    
    /**
     * Fetch and save cities from AirLab API
     * GET /api/airlab/fetch/cities
     */
    @GetMapping("/fetch/cities")
    public ResponseEntity<Map<String, Object>> fetchCities() {
        try {
            List<Cities> cities = airLabService.fetchAndSaveCities();
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Successfully fetched and saved cities");
            response.put("count", cities.size());
            response.put("data", cities);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Failed to fetch cities from AirLab API");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());
            errorResponse.put("status", 400);
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Get all cities from database
     * GET /api/airlab/cities
     */
    @GetMapping("/cities")
    public ResponseEntity<List<Cities>> getAllCities() {
        List<Cities> cities = airLabService.getAllCities();
        return ResponseEntity.ok(cities);
    }
}
