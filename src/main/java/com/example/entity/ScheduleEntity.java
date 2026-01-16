package com.example.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
public class ScheduleEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "flight_iata")
    private String flightIata;
    
    @Column(name = "airline_iata")
    private String airlineIata;
    
    @Column(name = "airline_name")
    private String airlineName;
    
    @Column(name = "dep_iata")
    private String depIata;
    
    @Column(name = "dep_airport_name")
    private String depAirportName;
    
    @Column(name = "arr_iata")
    private String arrIata;
    
    @Column(name = "arr_airport_name")
    private String arrAirportName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFlightIata() { return flightIata; }
    public void setFlightIata(String flightIata) { this.flightIata = flightIata; }
    
    public String getAirlineIata() { return airlineIata; }
    public void setAirlineIata(String airlineIata) { this.airlineIata = airlineIata; }
    
    public String getAirlineName() { return airlineName; }
    public void setAirlineName(String airlineName) { this.airlineName = airlineName; }
    
    public String getDepIata() { return depIata; }
    public void setDepIata(String depIata) { this.depIata = depIata; }
    
    public String getDepAirportName() { return depAirportName; }
    public void setDepAirportName(String depAirportName) { this.depAirportName = depAirportName; }
    
    public String getArrIata() { return arrIata; }
    public void setArrIata(String arrIata) { this.arrIata = arrIata; }
    
    public String getArrAirportName() { return arrAirportName; }
    public void setArrAirportName(String arrAirportName) { this.arrAirportName = arrAirportName; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
