package com.example.emailservice.services;

import com.example.emailservice.entities.Answer;
import com.example.emailservice.entities.Incident;
import com.example.emailservice.entities.Question;
import com.example.emailservice.repositories.AnswerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetAnswersService {

    private final AnswerRepository answerRepository;

    @Autowired
    public GetAnswersService(AnswerRepository answerRepository) {
        this.answerRepository = answerRepository;
    }

    /**
     * Retrieves answers related to a specific question and maps them to a response format.
     */
    public List<Map<String, Object>> getAnswersForQuestion(Question question) {
        List<Answer> answers = answerRepository.findByQuestionOrderByAnswerID(question);
        return mapAnswersToResponse(answers);
    }

    /**
     * Retrieves answers for a specific question within a given date range and maps them to a response format.
     */
    public List<Map<String, Object>> getAnswersForQuestionWithinDateRange(Question question, LocalDate startDate, LocalDate endDate) {
        List<Answer> answers = answerRepository.findByQuestionAndAnswerDateBetweenOrderByAnswerID(question, startDate, endDate);
        return mapAnswersToResponse(answers);
    }

    /**
     * Maps a list of answers to a response format suitable for clients.
     */
    private List<Map<String, Object>> mapAnswersToResponse(List<Answer> answers) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Answer answer : answers) {
            Incident incident = answer.getIncident();
            String emailDomain = extractEmailDomain(incident.getClient().getEmail());
            Map<String, Object> answerMap = new HashMap<>();
            answerMap.put("date", answer.getAnswerDate());
            answerMap.put("ehelpdesk", incident.getEhelpdesk().getName());
            answerMap.put("incidentNumber", incident.getIncidentNumber());
            answerMap.put("user", emailDomain);
            answerMap.put("serviceman", formatServicemanName(incident.getServiceman().getFirstName(), incident.getServiceman().getLastName()));
            answerMap.put("value", answer.getAnswerValue());
            response.add(answerMap);
        }
        return response;
    }

    private String extractEmailDomain(String email) {
        return "@" + email.split("@")[1];
    }

    private String formatServicemanName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }
}
