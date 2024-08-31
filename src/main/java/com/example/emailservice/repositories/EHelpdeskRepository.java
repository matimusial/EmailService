package com.example.emailservice.repositories;

import com.example.emailservice.entities.EHelpdesk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EHelpdeskRepository extends JpaRepository<EHelpdesk, Long> {
}
