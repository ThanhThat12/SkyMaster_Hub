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

-- Table: flights (original table)
CREATE TABLE IF NOT EXISTS flights (
    id INT AUTO_INCREMENT PRIMARY KEY,
    flight_iata VARCHAR(10),
    airline_iata VARCHAR(3),
    departure_airport VARCHAR(10),
    arrival_airport VARCHAR(10),
    departure_time BIGINT,
    arrival_time BIGINT,
    estimated_arrival_time BIGINT,
    status VARCHAR(50),
    duration INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_airline (airline_iata),
    INDEX idx_departure (departure_airport),
    INDEX idx_arrival (arrival_airport),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Example queries:
--
-- Check total schedules saved:
-- SELECT COUNT(*) FROM schedules;
--
-- View schedules by destination:
-- SELECT * FROM schedules WHERE arr_iata = 'JFK' ORDER BY created_at DESC;
--
-- Count schedules by destination:
-- SELECT arr_iata, arr_airport_name, COUNT(*) as total_flights 
-- FROM schedules 
-- GROUP BY arr_iata, arr_airport_name 
-- ORDER BY total_flights DESC;
--
-- Check cache stats:
-- SELECT 
--     (SELECT COUNT(*) FROM airlines) as total_airlines,
--     (SELECT COUNT(*) FROM airports) as total_airports,
--     (SELECT COUNT(*) FROM schedules) as total_schedules;
--
-- View recently added schedules:
-- SELECT * FROM schedules ORDER BY created_at DESC LIMIT 20;
--
-- Clear schedules (if needed):
-- TRUNCATE TABLE schedules;

-- ===================================================================
-- FLIGHT DELAY CACHE QUERIES (Simple version - no cache_key)
-- ===================================================================

-- View all cached queries:
-- SELECT query_type, iata_code, min_delay, 
--        COUNT(*) as flights, 
--        MAX(cached_at) as last_cached
-- FROM flight_delay_cache
-- GROUP BY query_type, iata_code, min_delay
-- ORDER BY last_cached DESC;

-- View cached flights for specific query:
-- SELECT * FROM flight_delay_cache 
-- WHERE query_type = 'departures' 
--   AND iata_code = 'JFK' 
--   AND min_delay = 30
-- ORDER BY cached_at DESC;

-- Check cache freshness:
-- SELECT query_type, iata_code, min_delay,
--        TIMESTAMPDIFF(MINUTE, MAX(cached_at), NOW()) as age_minutes,
--        COUNT(*) as flights
-- FROM flight_delay_cache
-- GROUP BY query_type, iata_code, min_delay
-- ORDER BY age_minutes;

-- Clean expired cache (older than 5 minutes):
-- DELETE FROM flight_delay_cache 
-- WHERE TIMESTAMPDIFF(MINUTE, cached_at, NOW()) >= 5;

-- Clear specific query cache:
-- DELETE FROM flight_delay_cache
-- WHERE query_type = 'departures' 
--   AND iata_code = 'JFK' 
--   AND min_delay = 30;

-- Clear all cache:
-- TRUNCATE TABLE flight_delay_cache;
