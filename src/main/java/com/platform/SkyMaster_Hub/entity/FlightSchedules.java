package com.platform.SkyMaster_Hub.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "flight_schedules", indexes = {
    @Index(name = "idx_dep_iata", columnList = "dep_iata"),
    @Index(name = "idx_fetch_at", columnList = "fetch_at")
})
@Data
public class FlightSchedules {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_iata")
    private String flightIata;

    @Column(name = "flight_icao")
    private String flightIcao;

    @Column(name = "airline_iata")
    private String airlineIata;

    @Column(name = "airline_icao")
    private String airlineIcao;

    @Column(name = "dep_iata")
    private String depIata;

    @Column(name = "dep_icao")
    private String depIcao;

    @Column(name = "dep_time")
    private String depTime;

    @Column(name = "arr_iata")
    private String arrIata;

    @Column(name = "arr_icao")
    private String arrIcao;

    @Column(name = "arr_time")
    private String arrTime;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "status")
    private String status;

    @Column(name = "fetch_at")
    private LocalDateTime fetchAt;
}
