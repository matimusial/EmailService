package com.example.emailservice.controllers;

import com.example.emailservice.entities.EHelpdesk;
import com.example.emailservice.entities.Question;
import com.example.emailservice.repositories.EHelpdeskRepository;
import com.example.emailservice.repositories.IncidentRepository;
import com.example.emailservice.repositories.QuestionRepository;
import com.example.emailservice.services.admin.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private final EHelpdeskRepository eHelpdeskRepository;
    private final QuestionRepository questionRepository;
    private final IncidentRepository incidentRepository;
    private final AdminService adminService;

    @Autowired
    public AdminController(EHelpdeskRepository eHelpdeskRepository,
                           QuestionRepository questionRepository,
                           IncidentRepository incidentRepository,
                           AdminService adminService) {
        this.eHelpdeskRepository = eHelpdeskRepository;
        this.questionRepository = questionRepository;
        this.incidentRepository = incidentRepository;
        this.adminService = adminService;
    }

    /**
     * Returns the currently authenticated user.
     */
    @GetMapping("/me")
    public ResponseEntity<String> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && !ANONYMOUS_USER.equals(authentication.getName())) {
            return ResponseEntity.ok(authentication.getName());
        } else {
            return ResponseEntity.ok(ANONYMOUS_USER);
        }
    }


    /**
     * Retrieves all EHelpdesk entities.
     */
    @GetMapping("/ehelpdesks")
    public ResponseEntity<List<EHelpdesk>> getAllEHelpdesks() {
        List<EHelpdesk> eHelpdesks = eHelpdeskRepository.findAll();
        return ResponseEntity.ok(eHelpdesks);
    }

    /**
     * Creates a new EHelpdesk entity.
     */
    @PostMapping("/submitEhelpdesk")
    public ResponseEntity<Void> createEHelpdesk(@RequestBody Map<String, Object> formData) {
        EHelpdesk eHelpdesk = new EHelpdesk();
        eHelpdesk.setName((String) formData.get("name"));
        try {
            eHelpdeskRepository.save(eHelpdesk);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves all questions along with related statistics.
     */
    @GetMapping("/questions")
    public ResponseEntity<Map<String, Object>> getAllQuestionsWithStatistics() {
        Map<String, Object> response = new HashMap<>();
        List<Question> questions = questionRepository.findAll();
        response.put("questions", questions);
        response.put("sent", incidentRepository.count());
        response.put("filled", incidentRepository.countByIsDoneTrue());
        return ResponseEntity.ok(response);
    }

    /**
     * Saves a list of questions provided in the request body.
     */
    @PostMapping("/submitQuestions")
    @Transactional
    public ResponseEntity<Void> saveQuestions(@RequestBody Map<String, Object> formData) {
        List<Map<String, Object>> questions = (List<Map<String, Object>>) formData.get("questions");
        for (Map<String, Object> questionData : questions) {
            try {
                questionRepository.save(adminService.saveQuestion(questionData));
            } catch (DataIntegrityViolationException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        }
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves details of a specific question along with related answers.
     */
    @GetMapping("/question/{id}")
    public ResponseEntity<Map<String, Object>> getQuestionDetailsWithAnswers(
            @PathVariable Long id,
            @RequestParam(value = "start_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "end_date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        Optional<Question> questionOpt = questionRepository.findById(id);

        if (questionOpt.isPresent()) {
            /**
             * Possible HTTP Status Codes:
             * - 200 OK: HashMap,
             * - 404 NOT FOUND: HashMap with message: Nie udzielono jeszcze żadnej odpowiedzi
             */
            return adminService.buildQuestionResponse(questionOpt.get(), startDate, endDate);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
