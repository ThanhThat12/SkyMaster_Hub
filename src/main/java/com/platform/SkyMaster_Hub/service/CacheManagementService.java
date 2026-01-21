package com.platform.SkyMaster_Hub.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.stereotype.Service;

@Service
public class CacheManagementService {
    private final CacheManager cacheManager;

    public CacheManagementService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // Get all cache keys in flightSchedules cache

    public Set<Object> getCacheKeys(String cacheName){
        ConcurrentMapCache cache = (ConcurrentMapCache) cacheManager.getCache(cacheName);

        if(cache != null){
            return cache.getNativeCache().keySet();
        }
        return Collections.emptySet();
    }


    // Get all cache flightschedules data
    public Map<Object, Object> getAllCacheData(){
        ConcurrentMapCache cache = (ConcurrentMapCache)cacheManager.getCache("flightSchedules");

        if(cache != null){
            return new HashMap<>(cache.getNativeCache());
        }
        return Collections.emptyMap();
        
    }
}
