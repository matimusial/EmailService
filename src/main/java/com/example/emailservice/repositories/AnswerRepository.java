package com.example.emailservice.repositories;

import com.example.emailservice.entities.Answer;
import com.example.emailservice.entities.Incident;
import com.example.emailservice.entities.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByIncidentAndQuestion(Incident incident, Question question);
    List<Answer> findByQuestionOrderByAnswerID(Question question);
    List<Answer> findByQuestionAndAnswerDateBetweenOrderByAnswerID(
            Question question,
            LocalDate startDate,
            LocalDate endDate
    );
}