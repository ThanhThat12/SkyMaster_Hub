package com.example.dto;

/**
 * Request DTO for fetching delayed flights by airport
 */
public class FetchDelayRequest {
    private String type;  // "departures" or "arrivals"
    private String iataCode;
    private int minDelay = 30;

    public FetchDelayRequest() {
    }

    public FetchDelayRequest(String type, String iataCode, int minDelay) {
        this.type = type;
        this.iataCode = iataCode;
        this.minDelay = minDelay;
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

    public int getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(int minDelay) {
        this.minDelay = minDelay;
    }
}
