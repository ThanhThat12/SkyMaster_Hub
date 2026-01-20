package com.platform.SkyMaster_Hub.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import com.platform.SkyMaster_Hub.mapper.FlightScheduleMapper;
import com.platform.SkyMaster_Hub.repository.FlightSchedulesRepository;
import org.springframework.beans.factory.annotation.Value;


@Service 
public class FlightScheduleService {

    private final WebClient.Builder webClientBuilder;
    private final FlightSchedulesRepository flightschedulesrepository;
    private final FlightScheduleMapper flightScheduleMapper;

    @Value("${airlab.api.key}")
    private String apiKey;
    @Value("${airlab.api.base-url}")
    private String baseUrl;

    public FlightScheduleService(Builder webClientBuilder, FlightSchedulesRepository flightschedulesrepository,
           FlightScheduleMapper flightScheduleMapper) {
        this.webClientBuilder = webClientBuilder;
        this.flightschedulesrepository = flightschedulesrepository;
        this.flightScheduleMapper = flightScheduleMapper;
    }

    

}
