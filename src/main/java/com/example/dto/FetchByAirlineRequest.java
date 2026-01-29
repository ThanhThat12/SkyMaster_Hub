package com.example.dto;

/**
 * Request DTO for fetching delayed flights by airline
 */
public class FetchByAirlineRequest {
    private String airlineIata;
    private int minDelay = 30;

    public FetchByAirlineRequest() {
    }

    public FetchByAirlineRequest(String airlineIata, int minDelay) {
        this.airlineIata = airlineIata;
        this.minDelay = minDelay;
    }

    // Getters and Setters
    public String getAirlineIata() {
        return airlineIata;
    }

    public void setAirlineIata(String airlineIata) {
        this.airlineIata = airlineIata;
    }

    public int getMinDelay() {
        return minDelay;
    }

    public void setMinDelay(int minDelay) {
        this.minDelay = minDelay;
    }
}
