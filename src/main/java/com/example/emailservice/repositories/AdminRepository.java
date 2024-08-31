package com.example.emailservice.repositories;

import com.example.emailservice.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long>{
    Optional<Admin> findByEmail(String email);
    Optional<Admin> findByUsername(String username);
    Optional<Admin> findByConfirmationCode(Integer confirmationCode);
    Optional<Admin> findByEmailAndConfirmationCode(String email, Integer confirmationCode);
    void deleteByUsername(String username);
    List<Admin> findByIsAuthorizedFalseAndConfirmationCodeExpiryBefore(LocalDateTime currentTime);
}