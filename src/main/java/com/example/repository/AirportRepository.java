package com.example.repository;

import com.example.entity.AirportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirportRepository extends JpaRepository<AirportEntity, String> {
    // Spring Data JPA tự động implement findById, findAll, save, etc.
}
