package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  
public class FlightSchedulesApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(FlightSchedulesApplication.class, args);
        System.out.println("\n" + "=".repeat(60));
        System.out.println(" Flight Schedules Web App is running!");
        System.out.println(" Open: http://localhost:8080");
        System.out.println("=".repeat(60) + "\n");
    }
}
