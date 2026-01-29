package com.example.dto;

/**
 * Request DTO for searching delayed flights
 */
public class SearchDelayRequest {
    private String iataCode;
    private String airline;
    private String depIata;

    public SearchDelayRequest() {
    }

    public SearchDelayRequest(String iataCode, String airline, String depIata) {
        this.iataCode = iataCode;
        this.airline = airline;
        this.depIata = depIata;
    }

    // Getters and Setters
    public String getIataCode() {
        return iataCode;
    }

    public void setIataCode(String iataCode) {
        this.iataCode = iataCode;
    }

    public String getAirline() {
        return airline;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public String getDepIata() {
        return depIata;
    }

    public void setDepIata(String depIata) {
        this.depIata = depIata;
    }
}
