package com.example.emailservice.services.admin;

import com.example.emailservice.entities.Question;
import com.example.emailservice.entities.QuestionType;
import com.example.emailservice.repositories.IncidentRepository;
import com.example.emailservice.services.GetAnswersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final IncidentRepository incidentRepository;
    private final GetAnswersService getAnswersService;

    @Autowired
    public AdminService(IncidentRepository incidentRepository,
                        GetAnswersService getAnswersService) {
        this.incidentRepository = incidentRepository;
        this.getAnswersService = getAnswersService;
    }

    /**
     * Saves a question entity based on the provided data.
     */
    public Question saveQuestion(Map<String, Object> questionData) {
        Integer questionVersion = Integer.valueOf(String.valueOf(questionData.get("version")));
        String questionText = (String) questionData.get("questionText");
        QuestionType questionType = QuestionType.valueOf((String) questionData.get("questionType"));
        Question question = new Question();
        question.setVersion(questionVersion);
        question.setQuestionText(questionText);
        question.setQuestionType(questionType);
        return question;
    }

    /**
     * Builds a response for a specific question, including related answers.
     */
    public ResponseEntity<Map<String, Object>> buildQuestionResponse(Question question, LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> answers;
        if (startDate != null || endDate != null) {
            answers = getAnswersService.getAnswersForQuestionWithinDateRange(question, startDate, endDate);
        } else {
            answers = getAnswersService.getAnswersForQuestion(question);
        }

        Integer questionVersion = question.getVersion();
        Map<String, Object> response = new HashMap<>();
        response.put("id", question.getQuestionID());
        response.put("version", questionVersion);
        response.put("questionText", question.getQuestionText());
        response.put("questionType", question.getQuestionType());
        response.put("sent", incidentRepository.countByQuestionVersion(questionVersion));
        response.put("filled", incidentRepository.countByQuestionVersionAndIsDoneTrue(questionVersion));

        if (answers.isEmpty()) {
            response.put("message", "Nie udzielono jeszcze żadnej odpowiedzi");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } else {
            response.put("answers", answers);
        }
        return ResponseEntity.ok(response);
    }
}
