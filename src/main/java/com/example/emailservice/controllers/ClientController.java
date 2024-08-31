package com.example.emailservice.controllers;

import com.example.emailservice.entities.*;
import com.example.emailservice.models.FormDataDTO;
import com.example.emailservice.repositories.AnswerRepository;
import com.example.emailservice.repositories.IncidentRepository;
import com.example.emailservice.repositories.QuestionRepository;
import com.example.emailservice.services.incident.IncidentFormProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class ClientController {

    private final IncidentRepository incidentRepository;
    private final IncidentFormProcessingService incidentFormProcessingService;
    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public ClientController(IncidentRepository incidentRepository,
                            IncidentFormProcessingService incidentFormProcessingService,
                            AnswerRepository answerRepository,
                            QuestionRepository questionRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentFormProcessingService = incidentFormProcessingService;
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Retrieves form details by hash asynchronously.
     */
    @Async
    @GetMapping("/form/{hash}")
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getFormDetailsByHash(@PathVariable String hash) {
        return CompletableFuture.supplyAsync(() -> {
            Optional<Incident> incidentOpt = incidentRepository.findByHash(hash);
            if (incidentOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Formularz wygasł");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            Incident incident = incidentOpt.get();
            if (incident.getIsDone()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Formularz został wysłany");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            return ResponseEntity.ok(incidentFormProcessingService.prepareFormData(incident));
        });
    }

    /**
     * Submits the form asynchronously and saves the answers.
     */
    @Async
    @PostMapping("/form/submit")
    @Transactional
    public CompletableFuture<ResponseEntity<String>> submitForm(@RequestBody Map<String, Object> formData) {
        return CompletableFuture.supplyAsync(() -> {
            String hash = (String) formData.get("hash");
            Optional<Incident> incidentOpt = incidentRepository.findByHash(hash);
            if (incidentOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            Incident incident = incidentOpt.get();
            incident.setIsDone(true);

            Map<String, String> answers = (Map<String, String>) formData.get("answers");

            for (Map.Entry<String, String> entry : answers.entrySet()) {
                Long questionId = Long.valueOf(entry.getKey());
                String answerValue = String.valueOf(entry.getValue());

                Question question = questionRepository.findById(questionId)
                        .orElseThrow(() -> new IllegalArgumentException("Question ID error: " + questionId));

                Answer answer = answerRepository.findByIncidentAndQuestion(incident, question)
                        .orElse(new Answer());

                answer.setAnswerValue(answerValue);
                answer.setIncident(incident);
                answer.setQuestion(question);
                answer.setAnswerDate(LocalDate.now());

                try {
                    answerRepository.save(answer);
                } catch (DataIntegrityViolationException e) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).build();
                }
            }
            try {
                incidentRepository.save(incident);
            } catch (DataIntegrityViolationException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
            return ResponseEntity.ok("Formularz został wysłany");
        });
    }

    /**
     * Processes the form data and sends the form asynchronously.
     * All responses in this service are directed to the eHelpDesk API.
     */
    @Async
    @PostMapping("/form/sendform")
    public CompletableFuture<ResponseEntity<String>> processAndSendForm(@RequestBody FormDataDTO formData) {
        return CompletableFuture.supplyAsync(() -> {
            for (Field field : FormDataDTO.class.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(formData);
                    if (value == null || (value instanceof String && ((String) value).isEmpty())) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: All fields are required and cannot be left empty");
                    }
                } catch (IllegalAccessException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: Failed to access the data");
                }
            }
            /**
             * Possible HTTP Status Codes:
             * - 200 OK,
             * - 400 BAD REQUEST,
             * - 409 CONFLICT,
             * - 500 INTERNAL SERVER ERROR
             */
            return incidentFormProcessingService.processIncidentForm(formData);
        });
    }
}
