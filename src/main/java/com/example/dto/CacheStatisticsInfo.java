package com.example.dto;

import com.github.benmanes.caffeine.cache.stats.CacheStats;

/**
 * DTO for overall cache statistics
 */
public class CacheStatisticsInfo {
    private long size;
    private long capacity;
    private long hitCount;
    private long missCount;
    private double hitRate;
    private double missRate;
    private long evictionCount;
    private long loadSuccessCount;
    private long loadFailureCount;
    private double averageLoadPenaltyMs;
    
    public CacheStatisticsInfo() {}
    
    public static CacheStatisticsInfo fromCaffeineStats(CacheStats stats, long size, long capacity) {
        CacheStatisticsInfo info = new CacheStatisticsInfo();
        info.size = size;
        info.capacity = capacity;
        info.hitCount = stats.hitCount();
        info.missCount = stats.missCount();
        info.hitRate = stats.hitRate() * 100;
        info.missRate = stats.missRate() * 100;
        info.evictionCount = stats.evictionCount();
        info.loadSuccessCount = stats.loadSuccessCount();
        info.loadFailureCount = stats.loadFailureCount();
        info.averageLoadPenaltyMs = stats.averageLoadPenalty() / 1_000_000.0; // ns to ms
        return info;
    }

    // Getters and Setters
    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getHitCount() {
        return hitCount;
    }

    public void setHitCount(long hitCount) {
        this.hitCount = hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public void setMissCount(long missCount) {
        this.missCount = missCount;
    }

    public double getHitRate() {
        return hitRate;
    }

    public void setHitRate(double hitRate) {
        this.hitRate = hitRate;
    }

    public double getMissRate() {
        return missRate;
    }

    public void setMissRate(double missRate) {
        this.missRate = missRate;
    }

    public long getEvictionCount() {
        return evictionCount;
    }

    public void setEvictionCount(long evictionCount) {
        this.evictionCount = evictionCount;
    }

    public long getLoadSuccessCount() {
        return loadSuccessCount;
    }

    public void setLoadSuccessCount(long loadSuccessCount) {
        this.loadSuccessCount = loadSuccessCount;
    }

    public long getLoadFailureCount() {
        return loadFailureCount;
    }

    public void setLoadFailureCount(long loadFailureCount) {
        this.loadFailureCount = loadFailureCount;
    }

    public double getAverageLoadPenaltyMs() {
        return averageLoadPenaltyMs;
    }

    public void setAverageLoadPenaltyMs(double averageLoadPenaltyMs) {
        this.averageLoadPenaltyMs = averageLoadPenaltyMs;
    }
    
    public double getUtilizationPercent() {
        return capacity > 0 ? (size * 100.0 / capacity) : 0;
    }
    
    public long getTotalRequests() {
        return hitCount + missCount;
    }
}
