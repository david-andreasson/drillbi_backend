package com.davanddev.drillbi_backend.controller;

import com.davanddev.drillbi_backend.dto.AnswerResponseDTO;
import com.davanddev.drillbi_backend.dto.QuizSessionDTO;
import com.davanddev.drillbi_backend.models.Question;
import com.davanddev.drillbi_backend.service.QuizSessionService;
import com.davanddev.drillbi_backend.session.QuizSession;
import com.davanddev.drillbi_backend.util.DtoMapper;
import org.springframework.web.bind.annotation.*;
import com.davanddev.drillbi_backend.dto.SessionStatsDTO;

@RestController
@RequestMapping("/api/v2/quiz")
public class QuizSessionController {

    private final QuizSessionService quizSessionService;

    public QuizSessionController(QuizSessionService quizSessionService) {
        this.quizSessionService = quizSessionService;
    }


    @PostMapping("/start")
    public QuizSessionDTO startQuiz(
            @RequestParam String courseName,
            @RequestParam(defaultValue = "ORDER") String orderType,
            @RequestParam(defaultValue = "0") int startQuestion
    ) {
        QuizSession session = quizSessionService.startSession(courseName, orderType, startQuestion);
        return DtoMapper.toSessionDTO(session);
    }



    @GetMapping("/next")
    public Object getNextQuestion(@RequestParam String sessionId) {
        Question nextQuestion = quizSessionService.getNextQuestion(sessionId);
        if (nextQuestion == null) {
            return "Quiz finished.";
        }
        return DtoMapper.toQuestionDTO(nextQuestion);
    }


    @PostMapping("/submit")
    public AnswerResponseDTO submitAnswer(@RequestParam String sessionId, @RequestParam String answer) {
        return quizSessionService.submitAnswer(sessionId, answer);
    }


    @GetMapping("/stats")
    public SessionStatsDTO getStats(@RequestParam String sessionId) {
        return quizSessionService.getSessionStats(sessionId);
    }
}
