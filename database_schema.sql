-- Create database if not exists
CREATE DATABASE IF NOT EXISTS airlab_db;
USE airlab_db;

-- Table: countries (Basic fields only)
CREATE TABLE IF NOT EXISTS countries (
    code VARCHAR(2) PRIMARY KEY,
    code3 VARCHAR(3),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: airlines (với country_code)
CREATE TABLE IF NOT EXISTS airlines (
    iata_code VARCHAR(3) PRIMARY KEY,
    icao_code VARCHAR(4),
    name VARCHAR(255) NOT NULL,
    country_code VARCHAR(2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_icao (icao_code),
    INDEX idx_country (country_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: airports (với country_code)
CREATE TABLE IF NOT EXISTS airports (
    iata_code VARCHAR(3) PRIMARY KEY,
    icao_code VARCHAR(4),
    name VARCHAR(255) NOT NULL,
    country_code VARCHAR(2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_icao (icao_code),
    INDEX idx_country (country_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Table: schedules (NEW - để lưu flight schedules)
CREATE TABLE IF NOT EXISTS schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    flight_iata VARCHAR(10),
    airline_iata VARCHAR(3),
    airline_name VARCHAR(255),
    dep_iata VARCHAR(3),
    dep_airport_name VARCHAR(255),
    arr_iata VARCHAR(3),
    arr_airport_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_flight (flight_iata),
    INDEX idx_airline (airline_iata),
    INDEX idx_dep (dep_iata),
    INDEX idx_arr (arr_iata)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE IF NOT EXISTS flight_delay (
    id INT AUTO_INCREMENT PRIMARY KEY,
    query_type VARCHAR(20) NOT NULL,     
    iata_code VARCHAR(3) NOT NULL,        
    min_delay INT NOT NULL,              
    airline_iata VARCHAR(3),
    flight_iata VARCHAR(10),
    flight_number VARCHAR(10),
    dep_iata VARCHAR(3),
    dep_time VARCHAR(50),
    arr_iata VARCHAR(3),
    arr_time VARCHAR(50),
    delay_minutes INT,
    cached_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_query (query_type, iata_code, min_delay),  
    INDEX idx_cached_at (cached_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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