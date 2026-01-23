package com.example.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.example.entity.DelayEntity;
import com.example.entity.RealtimeFlightEntity;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    @Bean(name = "flightDelayCache")
    public Cache<String, List<DelayEntity>> flightDelayCache() {
        return Caffeine.newBuilder()
                .maximumSize(3)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((String key, List<DelayEntity> value, RemovalCause cause) -> {
                    System.out.println(" Evicted from cache: " + key + " | Cause: " + cause +
                                     "  Size: " + (value != null ? value.size() : 0));
                    // Note: hitcountMap cleanup handled in FlightDelayService
                })
                .build();
    }

    
    // @Bean(name = "flightScheduleCache")
    // public Cache<String, List<ScheduleEntity>> flightScheduleCache() {
    //     return Caffeine.newBuilder()
    //             .maximumSize(200)
    //             .expireAfterWrite(5, TimeUnit.MINUTES)
    //             .recordStats()
    //             .removalListener((String key, List<ScheduleEntity> value, RemovalCause cause) -> {
    //                 System.out.println(" Schedule cache evicted: " + key + " | " + cause);
    //             })
    //             .build();
    // }

    
    @Bean(name = "searchCache")
    public Cache<String, List<DelayEntity>> searchCache() {
        return Caffeine.newBuilder()
                .maximumSize(50)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .removalListener((String key, List<DelayEntity> value, RemovalCause cause) -> {
                    
                })
                .build();
    }

    
    @Bean(name = "metadataCache")
    public Cache<String, String> metadataCache() {
        return Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterAccess(1, TimeUnit.HOURS)  
                .recordStats()
                .build();
    }

    @Bean(name = "realtimeFlightCache")
    public Cache<String, List<RealtimeFlightEntity>> realtimeFlightCache() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(5, TimeUnit.MINUTES)  // Real-time data needs shorter TTL
                .recordStats()
                .removalListener((String key, List<RealtimeFlightEntity> value, RemovalCause cause) -> {
                    System.out.println("  Realtime flight cache evicted: " + key + " | " + cause);
                })
                .build();
    }

   
    public static void printCacheStats(String cacheName, Cache<?, ?> cache) {
        CacheStats stats = cache.stats();
        
        double hitRate = stats.hitRate() * 100;
        double missRate = stats.missRate() * 100;
        
        System.out.println(" Cache Stats: " + cacheName);
        System.out.println("  Size: " + cache.estimatedSize());
        System.out.println("  Hits: " + stats.hitCount());
        System.out.println("  Misses: " + stats.missCount());
        System.out.println("  Hit Rate: " + String.format("%.2f%%", hitRate));
        System.out.println("  Miss Rate: " + String.format("%.2f%%", missRate));
        System.out.println("  Evictions: " + stats.evictionCount());
        System.out.println("  Load Time: " + stats.averageLoadPenalty() / 1_000_000 + "ms");
    }
}
