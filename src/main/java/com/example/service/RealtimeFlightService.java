package com.example.service;

import com.example.config.CaffeineConfig;
import com.example.entity.RealtimeFlightEntity;
import com.example.repository.RealtimeFlightRepository;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class RealtimeFlightService {

    @Value("${airlabs.api.key}")
    private String API_KEY;

    private final RealtimeFlightRepository realtimeFlightRepository;
    private final Cache<String, List<RealtimeFlightEntity>> cache;
    
    // hit count
    private final ConcurrentHashMap<String, AtomicLong> hitcountMap = new ConcurrentHashMap<>();

    public RealtimeFlightService(
            RealtimeFlightRepository realtimeFlightRepository,
            @Qualifier("realtimeFlightCache") Cache<String, List<RealtimeFlightEntity>> cache) {
        this.realtimeFlightRepository = realtimeFlightRepository;
        this.cache = cache;
    }

    
    public List<RealtimeFlightEntity> getRealtimeFlights(String depIata) {
        String cacheKey = "realtime|" + depIata;
        
        try {
            List<RealtimeFlightEntity> flights = cache.get(cacheKey, key -> {
                System.out.println(" Cache MISS - Checking database for: " + depIata);
                
                // // checkdb
                // List<RealtimeFlightEntity> dbFlights = realtimeFlightRepository.findByDepIata(depIata.toUpperCase());
                
                // if (!dbFlights.isEmpty()) {
                //     System.out.println("DB HIT - Found " + dbFlights.size() + " realtime flights in database");
                //     return dbFlights;
                // }
                
                // dbmiss call api
                // System.out.println(" DB MISS - Fetching from API...");
                try {
                    List<RealtimeFlightEntity> fetchedFlights = fetchFromAPI(depIata);
                    
                    if (!fetchedFlights.isEmpty()) {
                        saveToDatabase(fetchedFlights);
                        System.out.println(" API SUCCESS - Fetched " + fetchedFlights.size() + " flights, saved to DB and cached");
                    } else {
                        System.out.println("  API returned empty results");
                    }
                    
                    return fetchedFlights;
                    
                } catch (Exception e) {
                    System.err.println(" API ERROR: " + e.getMessage());
                    return new ArrayList<>();
                }
            });
            
           
            if (flights != null && !flights.isEmpty()) {
                hitcountMap.computeIfAbsent(cacheKey, k -> new AtomicLong(0)).incrementAndGet();
                System.out.println("Cache HIT for: " + cacheKey + " | Hit count: " + hitcountMap.get(cacheKey).get());
            }
            
            return flights;
            
        } catch (Exception e) {
            System.err.println("Error in getRealtimeFlights: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<RealtimeFlightEntity> fetchFromAPI(String depIata) throws Exception {
        String url = String.format(
                "https://airlabs.co/api/v9/flights?dep_iata=%s&api_key=%s",
                depIata, API_KEY
        );

        System.out.println(" API URL: " + url);

        List<RealtimeFlightEntity> flights = new ArrayList<>();

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                JsonArray responseArray = jsonObject.getAsJsonArray("response");

                if (responseArray != null) {
                    for (JsonElement element : responseArray) {
                        JsonObject flightObj = element.getAsJsonObject();
                        RealtimeFlightEntity flight = new RealtimeFlightEntity();
                        
                        flight.setHex(getString(flightObj, "hex"));
                        flight.setRegNumber(getString(flightObj, "reg_number"));
                        flight.setFlag(getString(flightObj, "flag"));
                        flight.setLat(getDouble(flightObj, "lat"));
                        flight.setLng(getDouble(flightObj, "lng"));
                        flight.setAlt(getInteger(flightObj, "alt"));
                        flight.setDir(getDouble(flightObj, "dir"));
                        flight.setSpeed(getInteger(flightObj, "speed"));
                        flight.setvSpeed(getDouble(flightObj, "v_speed"));
                        flight.setFlightNumber(getString(flightObj, "flight_number"));
                        flight.setFlightIata(getString(flightObj, "flight_iata"));
                        flight.setDepIata(getString(flightObj, "dep_iata"));
                        flight.setArrIata(getString(flightObj, "arr_iata"));
                        flight.setAirlineIata(getString(flightObj, "airline_iata"));
                        flight.setAircraftIcao(getString(flightObj, "aircraft_icao"));
                        flight.setUpdated(getLong(flightObj, "updated"));
                        flight.setStatus(getString(flightObj, "status"));
                        flight.setType(getString(flightObj, "type"));
                        flight.setFetchedAt(Instant.now());
                        
                        flights.add(flight);
                    }
                }
            }
        }

        return flights;
    }

    private void saveToDatabase(List<RealtimeFlightEntity> flights) {
        int newRecords = 0;
        int updatedRecords = 0;

        for (RealtimeFlightEntity newFlight : flights) {
            // Find existing flight by flight_iata
            RealtimeFlightEntity existingFlight = realtimeFlightRepository.findByFlightIata(
                    newFlight.getFlightIata()
            );

            if (existingFlight != null) {
                // UPDATE: Flight exists, update realtime data
                existingFlight.setLat(newFlight.getLat());
                existingFlight.setLng(newFlight.getLng());
                existingFlight.setAlt(newFlight.getAlt());
                existingFlight.setDir(newFlight.getDir());
                existingFlight.setSpeed(newFlight.getSpeed());
                existingFlight.setvSpeed(newFlight.getvSpeed());
                existingFlight.setStatus(newFlight.getStatus());
                existingFlight.setUpdated(newFlight.getUpdated());
                existingFlight.setFetchedAt(newFlight.getFetchedAt());
                
                // Also update other fields that might change
                existingFlight.setArrIata(newFlight.getArrIata());
                existingFlight.setDepIata(newFlight.getDepIata());
                
                realtimeFlightRepository.save(existingFlight);
                updatedRecords++;
                
            } else {
                // INSERT: New flight, save it
                realtimeFlightRepository.save(newFlight);
                newRecords++;
            }
        }

        System.out.println(" Database: " + newRecords + " new, " + updatedRecords + " updated");
    }

    
    public void clearAllCache() {
        long sizeBefore = cache.estimatedSize();
        cache.invalidateAll();
        cache.cleanUp();
        hitcountMap.clear();
        System.out.println("Cleared all cache entries: " + sizeBefore + " removed");
        System.out.println("Cleared all hit counts");
    }

    public void invalidateCacheKey(String depIata) {
        String cacheKey = "realtime|" + depIata;
        cache.invalidate(cacheKey);
        hitcountMap.remove(cacheKey);
        System.out.println("Invalidated cache key: " + cacheKey);
        System.out.println("Cleared hit count for key: " + cacheKey);
    }

    //
    public void printCacheStats() {
        CaffeineConfig.printCacheStats("RealtimeFlightCache", cache);
    }

    // 
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void scheduledCleanup() {
        cache.cleanUp();
        cleanupOrphanedHitCounts();
        System.out.println("Scheduled cache cleanup executed for Realtime Flights");
        printCacheStats();
    }

    //
    private void cleanupOrphanedHitCounts() {
        int removed = 0;
        for (String key : hitcountMap.keySet()) {
            if (cache.getIfPresent(key) == null) {
                hitcountMap.remove(key);
                removed++;
            }
        }
        if (removed > 0) {
            System.out.println(" Cleaned " + removed + " orphaned hit counts");
        }
    }

    
    public Map<String, Object> getCacheInfo() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = cache.stats();
        
        List<Map<String, Object>> entries = new ArrayList<>();
        
        // Iterate over cache entries
        for (Map.Entry<String, List<RealtimeFlightEntity>> entry : cache.asMap().entrySet()) {
            String key = entry.getKey();
            Map<String, Object> entryInfo = new HashMap<>();
            
            entryInfo.put("key", key);
            entryInfo.put("flightCount", entry.getValue().size());
            entryInfo.put("sizeKB", entry.getValue().size() * 0.8); // Estimate size
            long hitCount = hitcountMap.getOrDefault(key, new AtomicLong(0)).get();
            entryInfo.put("hitCount", hitCount);
            entries.add(entryInfo);
        }
        
        // Build cache info
        Map<String, Object> cacheInfo = new HashMap<>();
        cacheInfo.put("size", cache.estimatedSize());
        cacheInfo.put("capacity", 100); // From CaffeineConfig
        cacheInfo.put("hitCount", stats.hitCount());
        cacheInfo.put("missCount", stats.missCount());
        cacheInfo.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
        cacheInfo.put("evictionCount", stats.evictionCount());
        cacheInfo.put("entries", entries);
        cacheInfo.put("utilizationPercent", (cache.estimatedSize() * 100.0 / 100));
        cacheInfo.put("ttl", "2 minutes");
        
        return cacheInfo;
    }

    // Helper methods
    private String getString(JsonObject obj, String memberName) {
        JsonElement elem = obj.get(memberName);
        return elem != null && !elem.isJsonNull() ? elem.getAsString() : null;
    }

    private Integer getInteger(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element != null && !element.isJsonNull()) {
            try {
                return element.getAsInt();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Long getLong(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element != null && !element.isJsonNull()) {
            try {
                return element.getAsLong();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    private Double getDouble(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element != null && !element.isJsonNull()) {
            try {
                return element.getAsDouble();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
