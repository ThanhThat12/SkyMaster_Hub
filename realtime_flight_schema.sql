-- ============================================
-- Real-time Flight Tracking Schema
-- ============================================
-- Table for storing real-time flight data
-- Created: 2025-01-16
-- ============================================

CREATE TABLE IF NOT EXISTS realtime_flight (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Aircraft identification
    hex VARCHAR(10),
    reg_number VARCHAR(20),
    flag VARCHAR(5),
    
    -- Position data
    lat DOUBLE,
    lng DOUBLE,
    alt INT,
    dir DOUBLE,
    
    -- Speed data
    speed INT,
    v_speed DOUBLE,
    
    -- Flight information (IATA only)
    flight_number VARCHAR(20),
    flight_iata VARCHAR(20),
    
    -- Departure (IATA only)
    dep_iata VARCHAR(10),
    
    -- Arrival (IATA only)
    arr_iata VARCHAR(10),
    
    -- Airline (IATA only)
    airline_iata VARCHAR(10),
    
    -- Aircraft type (keeping ICAO as it's standard)
    aircraft_icao VARCHAR(10),
    
    -- Timestamps
    updated BIGINT,  -- Unix timestamp from API
    fetched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,  -- When we fetched it
    
    -- Status
    status VARCHAR(20),
    type VARCHAR(20),
    
    -- Indexes for performance
    INDEX idx_dep_iata (dep_iata),
    INDEX idx_arr_iata (arr_iata),
    INDEX idx_airline_iata (airline_iata),
    INDEX idx_flight_iata (flight_iata),
    INDEX idx_status (status),
    INDEX idx_updated (updated),
    INDEX idx_duplicate (flight_iata, updated)
);

-- ============================================
-- Cleanup old data (optional scheduled job)
-- ============================================
-- Real-time data becomes stale quickly
-- Recommend deleting data older than 24 hours

-- Manual cleanup:
-- DELETE FROM realtime_flight WHERE fetched_at < DATE_SUB(NOW(), INTERVAL 24 HOUR);
