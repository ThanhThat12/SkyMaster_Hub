package com.platform.SkyMaster_Hub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.platform.SkyMaster_Hub.entity.Airports;

import java.util.List;
import java.util.Optional;

@Repository
public interface AirportsRepository extends JpaRepository<Airports, Long> {
    Optional<Airports> findByIcaoCode(String icaoCode);
    List<Airports> findByCountryCode(String countryCode);
    boolean existsByIcaoCode(String icaoCode);
}
