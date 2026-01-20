package com.platform.SkyMaster_Hub.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Bỏ qua các field không cần thiết
public class FlightScheduleDTO {

    @JsonProperty("flight_iata")
    private String flightIata;

    @JsonProperty("flight_icao")
    private String flightIcao;
    
    @JsonProperty("flight_number")
    private String flightNumber;
    
    @JsonProperty("airline_iata")
    private String airlineIata;
    
    @JsonProperty("airline_icao")
    private String airlineIcao;
    
    @JsonProperty("dep_iata")
    private String depIata;

    @JsonProperty("dep_icao")
    private String depIcao;

    @JsonProperty("dep_time")
    private String depTime;

    @JsonProperty("dep_time_utc")
    private String depTimeUtc;
    
    @JsonProperty("arr_iata")
    private String arrIata;
    
    @JsonProperty("arr_icao")
    private String arrIcao;

    @JsonProperty("arr_time")
    private String arrTime;

    @JsonProperty("arr_time_utc")
    private String arrTimeUtc;

    @JsonProperty("duration")
    private Integer duration;

    @JsonProperty("cs_airline_iata")
    private String csAirlineIata;

    @JsonProperty("cs_flight_number")
    private String csFlightNumber;

    @JsonProperty("cs_flight_iata")
    private String csFlightIata;
    
    @JsonProperty("status")
    private String status;
}
