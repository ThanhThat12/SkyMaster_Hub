package com.example.dto;

/**
 * Request DTO for fetching realtime flights
 */
public class FetchRealtimeRequest {
    private String type;  // "dep" or "arr"
    private String iataCode;

    public FetchRealtimeRequest() {
    }

    public FetchRealtimeRequest(String type, String iataCode) {
        this.type = type;
        this.iataCode = iataCode;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIataCode() {
        return iataCode;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }
}
