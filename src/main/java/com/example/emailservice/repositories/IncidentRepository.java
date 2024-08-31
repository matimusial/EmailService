package com.example.emailservice.repositories;

import com.example.emailservice.entities.Incident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findByHash(String hash);
    Optional<Incident> findByIncidentNumberAndEhelpdesk_Id(String incidentNumber, Long ehelpdeskId);
    long countByIsDoneTrue();
    long countByQuestionVersion(Integer questionVersion);
    long countByQuestionVersionAndIsDoneTrue(Integer questionVersion);

}