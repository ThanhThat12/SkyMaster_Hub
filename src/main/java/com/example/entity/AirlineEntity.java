package com.example.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "airlines")
public class AirlineEntity {
    
    @Id
    @Column(name = "iata_code")
    private String iataCode;
    
    @Column(name = "icao_code")
    private String icaoCode;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "country_code")
    private String countryCode;

    // Getters and Setters
    public String getIataCode() { return iataCode; }
    public void setIataCode(String iataCode) { this.iataCode = iataCode; }
    
    public String getIcaoCode() { return icaoCode; }
    public void setIcaoCode(String icaoCode) { this.icaoCode = icaoCode; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}
