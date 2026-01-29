package com.example.service;

import com.example.config.CaffeineConfig;
import com.example.entity.DelayEntity;
import com.example.repository.DelayRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class FlightDelayService {

    @Value("${airlabs.api.key}")
    private String API_KEY;

    private final DelayRepository delayRepository;
    private final Cache<String, List<DelayEntity>> cache;
    private final Cache<String, List<DelayEntity>> searchCache;
    private final ConcurrentHashMap<String, AtomicLong> hitcountMap = new ConcurrentHashMap<>();
    public FlightDelayService(
            DelayRepository delayRepository,
            @Qualifier("flightDelayCache") Cache<String, List<DelayEntity>> cache,
            @Qualifier("searchCache") Cache<String, List<DelayEntity>> searchCache) {
        this.delayRepository = delayRepository;
        this.cache = cache;
        this.searchCache = searchCache;
        
        
    }

  
    public List<DelayEntity> getDelayedFlights(String type, String iataCode, int minDelay) {
        String cacheKey = buildCacheKey(type, iataCode, minDelay);
        
        try {            
            List<DelayEntity> flights = cache.get(cacheKey, key -> {
                System.out.println(" Cache MISS - Checking database for: " + cacheKey);
                
                //  Check db
                List<DelayEntity> dbFlights = queryDatabase(type, iataCode, minDelay);
                
                if (!dbFlights.isEmpty()) {
                    System.out.println(" DB HIT - Found " + dbFlights.size() + " flights in database");
                    return dbFlights; 
                }
                
                //db miss -> call api
                System.out.println(" DB MISS - Fetching from API...");
                try {
                    List<DelayEntity> fetchedFlights = fetchFromAPI(type, iataCode, minDelay);
                    
                    if (!fetchedFlights.isEmpty()) {
                        saveToDatabase(type, iataCode, minDelay, fetchedFlights);
                        System.out.println(" API SUCCESS - Fetched " + fetchedFlights.size() + " flights, saved to DB and cached");
                    } else {
                        System.out.println("  API returned empty results");
                    }
                    
                    return fetchedFlights;
                    
                } catch (Exception e) {
                    System.err.println(" API ERROR: " + e.getMessage());
                    return new ArrayList<DelayEntity>();
                }
            });
            
            
            if (flights != null && !flights.isEmpty()) {
                hitcountMap.computeIfAbsent(cacheKey, k -> new AtomicLong(0)).incrementAndGet();
            }
            
            return flights;
            
        } catch (Exception e) {
            System.err.println("Error in getDelayedFlights: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    
    public List<DelayEntity> searchDelayedFlights(String iataCode, String airline, String depIata) {
        String cacheKey = buildSearchCacheKey(iataCode, airline, depIata);
        
        return searchCache.get(cacheKey, key -> {
           
            
            System.out.println("   Querying database...");
            
            List<DelayEntity> result;
            if (iataCode != null && !iataCode.isEmpty()) {
                result = delayRepository.findByArrIata(iataCode.toUpperCase());
            } else if (airline != null && !airline.isEmpty()) {
                result = delayRepository.findByAirlineIata(airline.toUpperCase());
            } else if (depIata != null && !depIata.isEmpty()) {
                result = delayRepository.findByDepIata(depIata.toUpperCase());
            } else {
                result = delayRepository.findAllByOrderByIdDesc();
            }
            
            System.out.println("   Found " + result.size() + " flights from DB");
            return result;
        });
    }

    
    public List<DelayEntity> getDelaysByAirline(String airlineIata, int minDelay) {
    String cacheKey = buildAirlineCacheKey(airlineIata, minDelay);
    
    try {
        List<DelayEntity> flights = cache.get(cacheKey, key -> {
            System.out.println(" Cache MISS - Checking database for airline: " + airlineIata);
            
            // Check DB first
            List<DelayEntity> dbFlights = queryDatabaseByAirline(airlineIata, minDelay);
            
            if (!dbFlights.isEmpty()) {
                System.out.println(" DB HIT - Found " + dbFlights.size() + " flights in database");
                return dbFlights;
            }
            
            // DB miss -> call API
            System.out.println(" DB MISS - Fetching from API for airline: " + airlineIata);
            try {
                List<DelayEntity> fetchedFlights = fetchFromAPIByAirline(airlineIata, minDelay);
                
                if (!fetchedFlights.isEmpty()) {
                    saveToDatabase("airline", airlineIata, minDelay, fetchedFlights);
                    System.out.println(" API SUCCESS - Fetched " + fetchedFlights.size() + " flights, saved to DB");
                } else {
                    System.out.println(" API returned empty results for airline: " + airlineIata);
                }
                
                return fetchedFlights;
            } catch (Exception e) {
                System.err.println(" API ERROR: " + e.getMessage());
                return new ArrayList<>();
            }
        });
        
        if (flights != null && !flights.isEmpty()) {
            hitcountMap.computeIfAbsent(cacheKey, k -> new AtomicLong(0)).incrementAndGet();
        }
        
        return flights;
        
    } catch (Exception e) {
        System.err.println(" Error in getDelaysByAirline: " + e.getMessage());
        return new ArrayList<>();
    }
}

    private String buildAirlineCacheKey(String airlineIata, int minDelay)
    {
        return String.format("airline|%s|%d", airlineIata, minDelay);
    }

    private List<DelayEntity> fetchFromAPIByAirline(String airlineIata, int minDelay) throws Exception {
    String apiUrl = String.format(
        "https://airlabs.co/api/v9/delays?airline_iata=%s&delay=%d&api_key=%s",
        airlineIata.toUpperCase(),
        minDelay,
        API_KEY
    );
    
    System.out.println("📡 API URL: " + apiUrl.replace(API_KEY, "***"));
    
    try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
        HttpGet request = new HttpGet(apiUrl);
        
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            String jsonResponse = EntityUtils.toString(response.getEntity());
            
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            JsonArray responseArray = jsonObject.getAsJsonArray("response");
            
            if (responseArray == null || responseArray.isEmpty()) {
                return new ArrayList<>();
            }
            
            List<DelayEntity> flights = new ArrayList<>();
            for (JsonElement element : responseArray) {
                JsonObject flight = element.getAsJsonObject();
                
                DelayEntity entity = new DelayEntity();
                entity.setQueryType("airline");
                entity.setIataCode(airlineIata.toUpperCase());
                entity.setMinDelay(minDelay);
            
                entity.setAirlineIata(getString(flight, "airline_iata"));
                entity.setFlightIata(getString(flight, "flight_iata"));
                entity.setFlightNumber(getString(flight, "flight_number"));
                entity.setDepIata(getString(flight, "dep_iata"));
                entity.setDepTime(getString(flight, "dep_time"));
                entity.setArrIata(getString(flight, "arr_iata"));
                entity.setArrTime(getString(flight, "arr_time"));
                entity.setDelayMinutes(getInteger(flight, "delayed"));
                
                flights.add(entity);
            }
            
            return flights;
        }
    }
}

   
    public List<DelayEntity> getAllDelays() {
        return delayRepository.findAll();
    }
    
    /**
     * Get all stored delays (ordered by newest first)
     * This is for displaying all fetched delays
     */
    public List<DelayEntity> getAllStoredDelays() {
        return delayRepository.findAllByOrderByIdDesc();
    }
    
    /**
     * Count total stored delays in database
     */
    public long countStoredDelays() {
        return delayRepository.count();
    }

    
    public void clearAllCache() {
        long sizeBefore = cache.estimatedSize();
        cache.invalidateAll();
        cache.cleanUp();  
        hitcountMap.clear();  
        System.out.println("  Cleared all cache entries: " + sizeBefore + " removed");
        System.out.println("  Cleared all hit counts");
    }

    
    public void invalidateCacheKey(String type, String iataCode, int minDelay) {
        String cacheKey = buildCacheKey(type, iataCode, minDelay);
        cache.invalidate(cacheKey);
        hitcountMap.remove(cacheKey); 
        System.out.println("  Invalidated cache key: " + cacheKey);
        System.out.println("  Cleared hit count for key: " + cacheKey);
    }

    
    public void printCacheStats() {
        CaffeineConfig.printCacheStats("FlightDelayCache", cache);
    }

   
    @Scheduled(fixedRate = 10 * 60 * 1000) 
    public void scheduledCleanup() {
        cache.cleanUp();  
        cleanupOrphanedHitCounts();  
        System.out.println(" Scheduled cache cleanup executed");
        printCacheStats();
    }
    
    
    private void cleanupOrphanedHitCounts() {
        int removed = 0;
        for (String key : hitcountMap.keySet()) {
            if (cache.getIfPresent(key) == null) {
                hitcountMap.remove(key);
                removed++;
            }
        }
        if (removed > 0) {
            System.out.println(" Cleaned  " + removed + " orphaned hit counts");
        }
    }

    
    /**
     * Query database by type, iataCode and minDelay
     * This is TIER 2 in the 3-tier caching strategy
     */
    private List<DelayEntity> queryDatabase(String type, String iataCode, int minDelay) {
        List<DelayEntity> results;
        
        if (type.equalsIgnoreCase("departures")) {
            results = delayRepository.findByQueryTypeAndDepIataAndMinDelayGreaterThanEqual(
                type.toLowerCase(), 
                iataCode.toUpperCase(), 
                minDelay
            );
        } else {
            results = delayRepository.findByQueryTypeAndArrIataAndMinDelayGreaterThanEqual(
                type.toLowerCase(), 
                iataCode.toUpperCase(), 
                minDelay
            );
        }
        
        return results != null ? results : new ArrayList<>();
    }
    
    /**
     * Query database by airline IATA code
     */
    private List<DelayEntity> queryDatabaseByAirline(String airlineIata, int minDelay) {
        List<DelayEntity> results = delayRepository
            .findByAirlineIataAndMinDelayGreaterThanEqual(
            
                airlineIata.toUpperCase(),
                minDelay
            );
        
        return results != null ? results : new ArrayList<>();
    }
    
    private String buildCacheKey(String queryType, String iataCode, int minDelay) {
        return String.format("%s|%s|%d", queryType, iataCode, minDelay);
    }

    private String buildSearchCacheKey(String iataCode, String airline, String depIata) {
        return String.format("search|%s|%s|%s",
                iataCode != null ? iataCode : "null",
                airline != null ? airline : "null",
                depIata != null ? depIata : "null");
    }

    private List<DelayEntity> fetchFromAPI(String type, String iataCode, int minDelay) throws Exception {
        String paramName = type.equalsIgnoreCase("departures") ? "dep_iata" : "arr_iata";
        String url = String.format(
                "https://airlabs.co/api/v9/delays?%s=%s&delay=%d&type=%s&api_key=%s",
                paramName, iataCode, minDelay, type, API_KEY
        );

        System.out.println("    API URL: " + url);

        List<DelayEntity> flights = new ArrayList<>();

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            
            try (CloseableHttpResponse response = httpclient.execute(request)) {
                String jsonResponse = EntityUtils.toString(response.getEntity());
                JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                JsonArray responseArray = jsonObject.getAsJsonArray("response");

                if (responseArray != null) {
                    for (JsonElement element : responseArray) {
                        JsonObject delayObj = element.getAsJsonObject();
                        DelayEntity delayEntity = new DelayEntity();
                        delayEntity.setMinDelay(minDelay);
                        delayEntity.setIataCode(iataCode);
                        delayEntity.setAirlineIata(getString(delayObj, "airline_iata"));
                        delayEntity.setFlightIata(getString(delayObj, "flight_iata"));
                        delayEntity.setFlightNumber(getString(delayObj, "flight_number"));
                        delayEntity.setDepIata(getString(delayObj, "dep_iata"));
                        delayEntity.setDepTime(getString(delayObj, "dep_time"));
                        delayEntity.setArrIata(getString(delayObj, "arr_iata"));
                        delayEntity.setArrTime(getString(delayObj, "arr_time"));
                        delayEntity.setQueryType(type);
                        delayEntity.setDelayMinutes(getInteger(delayObj, "delayed"));
                        flights.add(delayEntity);
                    }
                }
            }
        }

        return flights;
    }

    private void saveToDatabase(String type, String iataCode, int minDelay, List<DelayEntity> flights) {
        int newRecords = 0;
        int skipRecords = 0;

        for (DelayEntity flight : flights) {
            boolean exists = delayRepository.existsByFlightIataAndDepTime(
                    flight.getFlightIata(),
                    flight.getDepTime()
            );

            if (exists) {
                skipRecords++;
                continue;
            }

            flight.setQueryType(type != null ? type.toLowerCase() : null);
            flight.setIataCode(iataCode != null ? iataCode.toUpperCase() : null);
            flight.setMinDelay(minDelay);
            delayRepository.save(flight);
            newRecords++;
        }

        System.out.println("    Database: " + newRecords + " new, " + skipRecords + " skipped");
        
        
        searchCache.invalidateAll();
        System.out.println("Invalidated search cache");
    }

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

    
    public java.util.Map<String, Object> getCacheInfo() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = cache.stats();
        
        java.util.List<java.util.Map<String, Object>> entries = new java.util.ArrayList<>();
        
        
        for (java.util.Map.Entry<String, List<DelayEntity>> entry : cache.asMap().entrySet()) {
            String key = entry.getKey();
            java.util.Map<String, Object> entryInfo = new java.util.HashMap<>();
            
            entryInfo.put("key", entry.getKey());
            entryInfo.put("flightCount", entry.getValue().size());
            entryInfo.put("sizeKB", entry.getValue().size() * 0.5); 
            long hitCount = hitcountMap.getOrDefault(key, new AtomicLong(0)).get();
            entryInfo.put("hitCount", hitCount);
            entries.add(entryInfo);
        }
        
   
        java.util.Map<String, Object> cacheInfo = new java.util.HashMap<>();
        cacheInfo.put("size", cache.estimatedSize());
        cacheInfo.put("capacity", 3);
        cacheInfo.put("hitCount", stats.hitCount());
        cacheInfo.put("missCount", stats.missCount());
        cacheInfo.put("hitRate", String.format("%.2f%%", stats.hitRate() * 100));
        cacheInfo.put("evictionCount", stats.evictionCount());
        cacheInfo.put("entries", entries);
        cacheInfo.put("utilizationPercent", (cache.estimatedSize() * 100.0 / 3));
        
        return cacheInfo;
    }
}
