package com.platform.SkyMaster_Hub.repository;

import com.platform.SkyMaster_Hub.entity.Cities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CitiesRepository extends JpaRepository<Cities, Long> {
    Optional<Cities> findByCityCode(String cityCode);
}
