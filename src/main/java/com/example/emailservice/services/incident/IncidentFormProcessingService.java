package com.example.emailservice.services.incident;

import com.example.emailservice.entities.EHelpdesk;
import com.example.emailservice.entities.Incident;
import com.example.emailservice.entities.Question;
import com.example.emailservice.models.FormDataDTO;
import com.example.emailservice.repositories.EHelpdeskRepository;
import com.example.emailservice.repositories.IncidentRepository;
import com.example.emailservice.repositories.QuestionRepository;
import com.example.emailservice.services.EmailNotificationService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class IncidentFormProcessingService {
    /**
     * Handles communication with the eHelpDesk API for all responses.
     */

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    private final EmailNotificationService emailNotificationService;
    private final IncidentRepository incidentRepository;
    private final IncidentDataService incidentDataService;
    private final EHelpdeskRepository eHelpdeskRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public IncidentFormProcessingService(EmailNotificationService emailNotificationService,
                                         IncidentRepository incidentRepository,
                                         IncidentDataService incidentDataService,
                                         EHelpdeskRepository eHelpdeskRepository,
                                         QuestionRepository questionRepository) {
        this.emailNotificationService = emailNotificationService;
        this.incidentRepository = incidentRepository;
        this.incidentDataService = incidentDataService;
        this.eHelpdeskRepository = eHelpdeskRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Validates and saves the incident form data, and sends a notification email to the client.
     */
    @Transactional
    public ResponseEntity<String> processIncidentForm(FormDataDTO formDataDTO) {

        Optional<Incident> incidentOpt = incidentRepository.findByIncidentNumberAndEhelpdesk_Id(formDataDTO.getIncidentNumber(), formDataDTO.getEhelpdeskId());
        Incident incident;

        if (incidentOpt.isPresent()) {
            incident = incidentOpt.get();
            if (incident.getIsDone()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Incident is already marked as done");
            }
        } else {
            incident = new Incident();
        }

        String hash = generateIncidentHash();
        String formUrl = generateFormUrl(hash);

        Optional<EHelpdesk> eHelpdeskOpt = eHelpdeskRepository.findById(formDataDTO.getEhelpdeskId());

        if (eHelpdeskOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid ehelpdesk version selected");
        }

        EHelpdesk eHelpdesk = eHelpdeskOpt.get();

        List<Question> questionList = questionRepository.findByVersion(formDataDTO.getQuestionVersion());

        if (questionList.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid question version selected");
        }

        ResponseEntity<String> response = incidentDataService.saveIncidentData(formDataDTO, incident, hash, eHelpdesk);
        if (response.getStatusCode() != HttpStatus.OK) {
            return response;
        }

        try {
            emailNotificationService.sendIncidentNotificationEmail(formDataDTO.getEmail(), formUrl, formDataDTO.getClientFirstName(), formDataDTO.getIncidentNumber(), eHelpdesk);
        } catch (MessagingException e) {
            String errorMessage = "Error: sending mail - " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
        return ResponseEntity.ok("Email sent successfully for: " + formDataDTO.getIncidentNumber());
    }

    /**
     * Constructs the data needed for the incident response form.
     */
    public Map<String, Object> prepareFormData(Incident incident) {
        List<Question> questions = questionRepository.findByVersion(incident.getQuestionVersion());

        List<Map<String, Object>> questionData = questions.stream().map(question -> {
            Map<String, Object> questionMap = new HashMap<>();
            questionMap.put("id", question.getQuestionID());
            questionMap.put("text", question.getQuestionText());
            questionMap.put("type", question.getQuestionType().name());
            return questionMap;
        }).collect(Collectors.toList());

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("incidentNumber", incident.getIncidentNumber());
        responseData.put("topic", incident.getIncidentTopic());
        responseData.put("technician", incident.getServiceman().getFirstName() + " " + incident.getServiceman().getLastName());
        responseData.put("firstName", incident.getClient().getFirstName());
        responseData.put("questions", questionData);

        return responseData;
    }

    private String generateFormUrl(String hash) {
        return frontendBaseUrl + "/form?hash=" + hash;
    }

    private static String generateIncidentHash() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }
}
