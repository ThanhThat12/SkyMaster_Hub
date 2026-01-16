package com.example.dto;

import java.time.LocalDateTime;

/**
 * DTO for cache entry information
 */
public class CacheEntryInfo {
    private String key;
    private int flightCount;
    private long estimatedSizeBytes;
    private LocalDateTime cachedAt;
    private long ageMinutes;
    private String status; // "active", "near-expiry", "stale"
    
    public CacheEntryInfo() {}
    
    public CacheEntryInfo(String key, int flightCount, long estimatedSizeBytes, 
                         LocalDateTime cachedAt, long ageMinutes, String status) {
        this.key = key;
        this.flightCount = flightCount;
        this.estimatedSizeBytes = estimatedSizeBytes;
        this.cachedAt = cachedAt;
        this.ageMinutes = ageMinutes;
        this.status = status;
    }

    // Getters and Setters
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getFlightCount() {
        return flightCount;
    }

    public void setFlightCount(int flightCount) {
        this.flightCount = flightCount;
    }

    public long getEstimatedSizeBytes() {
        return estimatedSizeBytes;
    }

    public void setEstimatedSizeBytes(long estimatedSizeBytes) {
        this.estimatedSizeBytes = estimatedSizeBytes;
    }

    public LocalDateTime getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(LocalDateTime cachedAt) {
        this.cachedAt = cachedAt;
    }

    public long getAgeMinutes() {
        return ageMinutes;
    }

    public void setAgeMinutes(long ageMinutes) {
        this.ageMinutes = ageMinutes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    // Convenience method for UI
    public String getFormattedSize() {
        if (estimatedSizeBytes < 1024) {
            return estimatedSizeBytes + " B";
        } else if (estimatedSizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", estimatedSizeBytes / 1024.0);
        } else {
            return String.format("%.2f MB", estimatedSizeBytes / (1024.0 * 1024.0));
        }
    }
}
