package com.example.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "realtime_flight")
public class RealtimeFlightEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Column(name = "hex")
    private String hex;
    
    @Column(name = "reg_number")
    private String regNumber;
    
    @Column(name = "flag")
    private String flag;
    
    @Column(name = "lat")
    private Double lat;
    
    @Column(name = "lng")
    private Double lng;
    
    @Column(name = "alt")
    private Integer alt;
    
    @Column(name = "dir")
    private Double dir;
    
    @Column(name = "speed")
    private Integer speed;
    
    @Column(name = "v_speed")
    private Double vSpeed;
    
    @Column(name = "flight_number")
    private String flightNumber;
    
    @Column(name = "flight_iata")
    private String flightIata;
    
    @Column(name = "dep_iata")
    private String depIata;
    
    @Column(name = "arr_iata")
    private String arrIata;
    
    @Column(name = "airline_iata")
    private String airlineIata;
    
    @Column(name = "aircraft_icao")
    private String aircraftIcao;
    
    @Column(name = "updated")
    private Long updated;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "type")
    private String type;
    
    @Column(name = "fetched_at")
    private Instant fetchedAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public String getRegNumber() {
        return regNumber;
    }

    public void setRegNumber(String regNumber) {
        this.regNumber = regNumber;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public Integer getAlt() {
        return alt;
    }

    public void setAlt(Integer alt) {
        this.alt = alt;
    }

    public Double getDir() {
        return dir;
    }

    public void setDir(Double dir) {
        this.dir = dir;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Double getvSpeed() {
        return vSpeed;
    }

    public void setvSpeed(Double vSpeed) {
        this.vSpeed = vSpeed;
    }

    public String getFlightNumber() {
        return flightNumber;
    }

    public void setFlightNumber(String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getFlightIata() {
        return flightIata;
    }

    public void setFlightIata(String flightIata) {
        this.flightIata = flightIata;
    }

    public String getDepIata() {
        return depIata;
    }

    public void setDepIata(String depIata) {
        this.depIata = depIata;
    }

    public String getArrIata() {
        return arrIata;
    }

    public void setArrIata(String arrIata) {
        this.arrIata = arrIata;
    }

    public String getAirlineIata() {
        return airlineIata;
    }

    public void setAirlineIata(String airlineIata) {
        this.airlineIata = airlineIata;
    }

    public String getAircraftIcao() {
        return aircraftIcao;
    }

    public void setAircraftIcao(String aircraftIcao) {
        this.aircraftIcao = aircraftIcao;
    }

    public Long getUpdated() {
        return updated;
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
