// package com.example.service;

// import com.example.config.CaffeineConfig;
// import com.example.entity.*;
// import com.example.repository.*;
// import com.github.benmanes.caffeine.cache.Cache;
// import com.google.gson.*;
// import org.apache.hc.client5.http.classic.methods.HttpGet;
// import org.apache.hc.client5.http.impl.classic.*;
// import org.apache.hc.core5.http.io.entity.EntityUtils;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import java.util.*;

// @Service
// public class FlightScheduleService {

//     @Value("${airlabs.api.key}")
//     private String apiKey;

//     @Value("${airlabs.api.base-url}")
//     private String baseUrl;

//     private final ScheduleRepository scheduleRepository;
//     private final AirlineRepository airlineRepository;
//     private final AirportRepository airportRepository;

//     private final Cache<String, List<ScheduleEntity>> scheduleCache;
//     private final Cache<String, String> metadataCache;

//     public FlightScheduleService(
//             ScheduleRepository scheduleRepository,
//             AirlineRepository airlineRepository,
//             AirportRepository airportRepository,
//             @Qualifier("flightScheduleCache") Cache<String, List<ScheduleEntity>> scheduleCache,
//             @Qualifier("metadataCache") Cache<String, String> metadataCache) {
        
//         this.scheduleRepository = scheduleRepository;
//         this.airlineRepository = airlineRepository;
//         this.airportRepository = airportRepository;
        
//         this.scheduleCache = scheduleCache;
//         this.metadataCache = metadataCache;

//         System.out.println(" FlightScheduleService initialized with Caffeine Caches");
//         System.out.println(" Schedule Cache: 200 entries, 5 min TTL");
//         System.out.println(" Metadata Cache: 1000 entries, 1 hour TTL");
        
//         loadMetadataCache();
//     }

//     /**
//      * Load airline and airport metadata into cache on startup
//      */
//     private void loadMetadataCache() {
//         int airlineCount = 0;
//         int airportCount = 0;

//         for (AirlineEntity airline : airlineRepository.findAll()) {
//             metadataCache.put("airline:" + airline.getIataCode(), airline.getName());
//             airlineCount++;
//         }

//         for (AirportEntity airport : airportRepository.findAll()) {
//             metadataCache.put("airport:" + airport.getIataCode(), airport.getName());
//             airportCount++;
//         }

//         System.out.println("📚 Loaded metadata cache: " + airlineCount + " airlines, " + airportCount + " airports");
//     }

//     /**
//      * Fetch and save schedules with Caffeine caching
//      */
//     @Transactional
//     public List<ScheduleEntity> fetchAndSaveSchedules(String arrIata) throws Exception {
//         String cacheKey = arrIata == null ? "ALL" : arrIata.toUpperCase();

//         try {
//             // Use Caffeine's get with mapping function
//             List<ScheduleEntity> schedules = scheduleCache.get(cacheKey, key -> {
//                 System.out.println(" Schedule cache MISS for: " + key);
//                 System.out.println("   Fetching from API...");

//                 try {
//                     return fetchSchedulesFromAPI(arrIata);
//                 } catch (Exception e) {
//                     System.err.println("   API fetch failed: " + e.getMessage());
//                     return new ArrayList<>();
//                 }
//             });

//             if (schedules != null && !schedules.isEmpty()) {
//                 System.out.println(" Cache HIT for schedules: " + cacheKey);
//                 System.out.println("    Returning " + schedules.size() + " cached schedules");
//             }

//             return schedules;

//         } catch (Exception e) {
//             System.err.println(" Error in fetchAndSaveSchedules: " + e.getMessage());
//             throw e;
//         }
//     }

//     /**
//      * Internal method to fetch schedules from API
//      */
//     private List<ScheduleEntity> fetchSchedulesFromAPI(String arrIata) throws Exception {
//         String url = String.format("%s/schedules?arr_iata=%s&api_key=%s",
//                 baseUrl, arrIata, apiKey);

//         System.out.println(" Fetching schedules for: " + arrIata);

//         List<ScheduleEntity> schedules = new ArrayList<>();
//         Set<String> newAirlines = new HashSet<>();
//         Set<String> newAirports = new HashSet<>();

//         try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//             HttpGet request = new HttpGet(url);

//             try (CloseableHttpResponse response = httpClient.execute(request)) {
//                 String jsonResponse = EntityUtils.toString(response.getEntity());
//                 JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();

//                 if (jsonObject.has("error")) {
//                     throw new Exception("API Error: " + jsonObject.getAsJsonObject("error")
//                             .get("message").getAsString());
//                 }

//                 JsonArray responseArray = jsonObject.getAsJsonArray("response");

//                 if (responseArray == null || responseArray.size() == 0) {
//                     System.out.println(" No flights found");
//                     return schedules;
//                 }

//                 System.out.println(" Found " + responseArray.size() + " flights");

//                 // Parse flight data
//                 for (JsonElement element : responseArray) {
//                     JsonObject flightObj = element.getAsJsonObject();

//                     String airlineIata = getString(flightObj, "airline_iata");
//                     String depIata = getString(flightObj, "dep_iata");
//                     String arrIataCode = getString(flightObj, "arr_iata");

//                     // Track missing metadata
//                     if (airlineIata != null && !hasMetadata("airline:" + airlineIata)) {
//                         newAirlines.add(airlineIata);
//                     }
//                     if (depIata != null && !hasMetadata("airport:" + depIata)) {
//                         newAirports.add(depIata);
//                     }
//                     if (arrIataCode != null && !hasMetadata("airport:" + arrIataCode)) {
//                         newAirports.add(arrIataCode);
//                     }

//                     ScheduleEntity schedule = new ScheduleEntity();
//                     schedule.setFlightIata(getString(flightObj, "flight_iata"));
//                     schedule.setAirlineIata(airlineIata);
//                     schedule.setDepIata(depIata);
//                     schedule.setArrIata(arrIataCode);

//                     schedules.add(schedule);
//                 }

//                 // Fetch missing metadata
//                 if (!newAirlines.isEmpty() || !newAirports.isEmpty()) {
//                     fetchMissingData(newAirlines, newAirports);
//                 }

//                 // Populate names from cache
//                 for (ScheduleEntity schedule : schedules) {
//                     schedule.setAirlineName(getMetadata("airline:" + schedule.getAirlineIata(), "Unknown"));
//                     schedule.setDepAirportName(getMetadata("airport:" + schedule.getDepIata(), "Unknown"));
//                     schedule.setArrAirportName(getMetadata("airport:" + schedule.getArrIata(), "Unknown"));
//                 }

//                 // Save to database
//                 scheduleRepository.saveAll(schedules);
//                 System.out.println(" Saved " + schedules.size() + " schedules to database");
//             }
//         }

//         return schedules;
//     }

//     /**
//      * Fetch missing airline/airport data
//      */
//     private void fetchMissingData(Set<String> airlines, Set<String> airports) throws Exception {
        
//         for (String iata : airlines) {
//             try {
//                 AirlineEntity airline = fetchAirline(iata);
//                 if (airline != null) {
//                     airlineRepository.save(airline);
//                     metadataCache.put("airline:" + airline.getIataCode(), airline.getName());
//                 }
//                 Thread.sleep(100);
//             } catch (Exception e) {
//                 System.err.println(" Error fetching airline " + iata);
//             }
//         }

//         // Fetch airports
//         for (String iata : airports) {
//             try {
//                 AirportEntity airport = fetchAirport(iata);
//                 if (airport != null) {
//                     airportRepository.save(airport);
//                     metadataCache.put("airport:" + airport.getIataCode(), airport.getName());
//                 }
//                 Thread.sleep(100);
//             } catch (Exception e) {
//                 System.err.println(" Error fetching airport " + iata);
//             }
//         }
//     }

//     /**
//      * Search schedules from database
//      */
//     public List<ScheduleEntity> searchSchedules(String arrIata, String airline, String depIata) {
//         if (arrIata != null && !arrIata.isEmpty()) {
//             return scheduleRepository.findByArrIata(arrIata.toUpperCase());
//         } else if (airline != null && !airline.isEmpty()) {
//             return scheduleRepository.findByAirlineIata(airline.toUpperCase());
//         } else if (depIata != null && !depIata.isEmpty()) {
//             return scheduleRepository.findByDepIata(depIata.toUpperCase());
//         }
//         return scheduleRepository.findAll();
//     }

//     /**
//      * Clear schedule cache
//      */
//     public void clearScheduleCache() {
//         long size = scheduleCache.estimatedSize();
//         scheduleCache.invalidateAll();
//         System.out.println("🗑️ Cleared schedule cache: " + size + " entries removed");
//     }

//     /**
//      * Print cache stats
//      */
//     public void printCacheStats() {
//         CaffeineConfig.printCacheStats("ScheduleCache", scheduleCache);
//         CaffeineConfig.printCacheStats("MetadataCache", metadataCache);
//     }

//     // ============================================
//     // HELPER METHODS
//     // ============================================

//     private boolean hasMetadata(String key) {
//         return metadataCache.getIfPresent(key) != null;
//     }

//     private String getMetadata(String key, String defaultValue) {
//         String value = metadataCache.getIfPresent(key);
//         return value != null ? value : defaultValue;
//     }

//     private AirlineEntity fetchAirline(String iata) throws Exception {
//         String url = String.format("%s/airlines?iata_code=%s&api_key=%s", baseUrl, iata, apiKey);

//         try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//             HttpGet request = new HttpGet(url);
//             try (CloseableHttpResponse response = httpClient.execute(request)) {
//                 String jsonResponse = EntityUtils.toString(response.getEntity());
//                 JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
//                 JsonArray responseArray = jsonObject.getAsJsonArray("response");

//                 if (responseArray != null && responseArray.size() > 0) {
//                     JsonObject obj = responseArray.get(0).getAsJsonObject();
//                     AirlineEntity airline = new AirlineEntity();
//                     airline.setIataCode(getString(obj, "iata_code"));
//                     airline.setName(getString(obj, "name"));
//                     airline.setIcaoCode(getString(obj, "icao_code"));
//                     airline.setCountryCode(getString(obj, "country_code"));
//                     return airline;
//                 }
//             }
//         }
//         return null;
//     }

//     private AirportEntity fetchAirport(String iata) throws Exception {
//         String url = String.format("%s/airports?iata_code=%s&api_key=%s", baseUrl, iata, apiKey);

//         try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
//             HttpGet request = new HttpGet(url);
//             try (CloseableHttpResponse response = httpClient.execute(request)) {
//                 String jsonResponse = EntityUtils.toString(response.getEntity());
//                 JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
//                 JsonArray responseArray = jsonObject.getAsJsonArray("response");

//                 if (responseArray != null && responseArray.size() > 0) {
//                     JsonObject obj = responseArray.get(0).getAsJsonObject();
//                     AirportEntity airport = new AirportEntity();
//                     airport.setIataCode(getString(obj, "iata_code"));
//                     airport.setName(getString(obj, "name"));
//                     airport.setIcaoCode(getString(obj, "icao_code"));
//                     airport.setCountryCode(getString(obj, "country_code"));
//                     return airport;
//                 }
//             }
//         }
//         return null;
//     }

//     private String getString(JsonObject obj, String key) {
//         JsonElement element = obj.get(key);
//         return (element != null && !element.isJsonNull()) ? element.getAsString() : null;
//     }
// }
