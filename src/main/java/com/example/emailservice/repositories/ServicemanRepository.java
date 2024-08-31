package com.example.emailservice.repositories;

import com.example.emailservice.entities.Serviceman;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ServicemanRepository extends JpaRepository<Serviceman, Long> {
    Optional<Serviceman> findByFirstNameAndLastName(String firstName, String lastName);
}
