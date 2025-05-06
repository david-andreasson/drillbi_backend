package com.davanddev.quizapp_api.controller;

import com.davanddev.quizapp_api.dto.AnswerResponseDTO;
import com.davanddev.quizapp_api.dto.QuizSessionDTO;
import com.davanddev.quizapp_api.models.Question;
import com.davanddev.quizapp_api.service.QuizSessionService;
import com.davanddev.quizapp_api.session.QuizSession;
import com.davanddev.quizapp_api.util.DtoMapper;
import org.springframework.web.bind.annotation.*;
import com.davanddev.quizapp_api.dto.SessionStatsDTO;

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
