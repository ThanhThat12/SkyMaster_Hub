package com.example.repository;

import com.example.entity.AirlineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AirlineRepository extends JpaRepository<AirlineEntity, String> {
    // Spring Data JPA tự động implement findById, findAll, save, etc.
}
