package com.platform.SkyMaster_Hub.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.platform.SkyMaster_Hub.dto.FlightScheduleDTO;
import com.platform.SkyMaster_Hub.dto.response.AirLabResponse;
import com.platform.SkyMaster_Hub.entity.FlightSchedules;
import com.platform.SkyMaster_Hub.exception.AirLabApiException;
import com.platform.SkyMaster_Hub.mapper.FlightScheduleMapper;
import com.platform.SkyMaster_Hub.repository.FlightSchedulesRepository;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;


@Service 
public class FlightScheduleService {

    private final WebClient.Builder webClientBuilder;
    private final FlightSchedulesRepository flightschedulesrepository;
    private final FlightScheduleMapper flightScheduleMapper;
    private final CacheManager cachemanager;

    @Value("${airlab.api.key}")
    private String apiKey;
    @Value("${airlab.api.base-url}")
    private String baseUrl;

    public FlightScheduleService(Builder webClientBuilder, FlightSchedulesRepository flightschedulesrepository,
            FlightScheduleMapper flightScheduleMapper, CacheManager cachemanager) {
        this.webClientBuilder = webClientBuilder;
        this.flightschedulesrepository = flightschedulesrepository;
        this.flightScheduleMapper = flightScheduleMapper;
        this.cachemanager = cachemanager;
    }

    public List<FlightScheduleDTO> getFlightSchedules(String depIata){

        List<FlightScheduleDTO> cachedData = getFromCache(depIata);
        if(cachedData != null && !cachedData.isEmpty()){
            return cachedData;
        }
         return fetchAndSaveFlightSchedules(depIata);
    }
    private List<FlightScheduleDTO> getFromCache(String depIata){
        Cache cache = cachemanager.getCache("flightSchedules");
        if(cache != null){
            Cache.ValueWrapper wrapper = cache.get(depIata);
            if(wrapper != null){
                return (List<FlightScheduleDTO>) wrapper.get();
            }
        }
        return null;
    }

    private void saveToCache(String depIata, List<FlightScheduleDTO> data){
        Cache cache = cachemanager.getCache("flightSchedules");
        if(cache != null){
            cache.put(depIata, data);
        }
    }

    public List<FlightScheduleDTO> fetchAndSaveFlightSchedules(String depIata){
        
            List<FlightSchedules> existondata = flightschedulesrepository.findByDepIata(depIata);

            if(!existondata.isEmpty()){
                saveToCache(depIata, existondata.stream()
                .map(flightScheduleMapper::toDTO)
                .collect(Collectors.toList()));
                return existondata.stream()
                .map(flightScheduleMapper::toDTO)
                .collect(Collectors.toList());
            }   
        try {
            WebClient webclient = webClientBuilder.baseUrl(baseUrl).build();

            String rawResponse = webclient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/schedules")
                    .queryParam("api_key", apiKey)
                    .queryParam("dep_iata", depIata)
                    .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

            if(rawResponse == null || rawResponse.isEmpty()){
                throw new AirLabApiException("No data received from AirLab API");
            }   

            AirLabResponse<FlightScheduleDTO> response;
            try{
                ObjectMapper mapper = new ObjectMapper();
                response = mapper.readValue(rawResponse, new TypeReference<AirLabResponse<FlightScheduleDTO>>(){});
            }
            catch(Exception parseException){
                throw new AirLabApiException("Failed to parse API response: " + parseException.getMessage());
            
            }

            flightschedulesrepository.deleteByDepIata(depIata);

            List<FlightSchedules> schedules = response.getResponse().stream()
                .map(flightScheduleMapper::toEntity)
                .collect(Collectors.toList());

            List<FlightSchedules> savedSchedules = flightschedulesrepository.saveAll(schedules);

            List<FlightScheduleDTO> dtoList = savedSchedules.stream()
                .map(flightScheduleMapper::toDTO)
                .collect(Collectors.toList());
            
            saveToCache(depIata, dtoList);

            return dtoList;
            }
        catch(Exception e){
            throw new AirLabApiException("Failed to fetch flight schedules from AirLab API: " + e.getMessage(), e);
        }
    }

        // public List<FlightScheduleDTO> forceRefreshFlightSchedules(String depIata){
        //     flightschedulesrepository.deleteByDepIata(depIata);
        //     return fetchAndSaveFlightSchedules(depIata);
        // }
        
    }
    


