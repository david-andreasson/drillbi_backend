package com.davanddev.drillbi_backend.service;

import com.davanddev.drillbi_backend.dto.AnswerResponseDTO;
import com.davanddev.drillbi_backend.dto.SessionStatsDTO;
import com.davanddev.drillbi_backend.models.Question;
import com.davanddev.drillbi_backend.models.QuestionOption;
import com.davanddev.drillbi_backend.session.QuizSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class QuizSessionService {

    private static final Logger logger = LoggerFactory.getLogger(QuizSessionService.class);

    private final Map<String, QuizSession> sessions = new ConcurrentHashMap<>();
    private final QuestionService questionService;

    public QuizSession startSession(String courseName, String orderType, int startQuestion) {
        logger.info("Starting new quiz session for course: {}, orderType: {}, startQuestion: {}", courseName, orderType, startQuestion);
        QuizSession session = new QuizSession(courseName, orderType, questionService.getQuestions(courseName, orderType));
        if (startQuestion >= 1 && startQuestion <= session.getTotalQuestions()) {
            session.setCurrentIndex(startQuestion - 1);
        }
        sessions.put(session.getSessionId(), session);
        return session;
    }

    public Question getNextQuestion(String sessionId) {
        QuizSession session = sessions.get(sessionId);
        if (session == null || session.getCurrentIndex() >= session.getTotalQuestions()) {
            return null;
        }
        return session.getQuestions().get(session.getCurrentIndex());
    }

    public AnswerResponseDTO submitAnswer(String sessionId, String answer) {
        QuizSession session = sessions.get(sessionId);
        if (session == null || session.getCurrentIndex() >= session.getTotalQuestions()) {
            return new AnswerResponseDTO(false, "incorrectFeedback", answer, "Unknown", getSessionStats(sessionId));
        }

        int currentIdx = session.getCurrentIndex();
        Question currentQuestion = session.getQuestions().get(currentIdx);
        session.getQuestions().remove(currentIdx);
        session.setAnsweredCount(session.getAnsweredCount() + 1);

        boolean isCorrect = answer.equalsIgnoreCase(currentQuestion.getCorrectOptionLabel());
        if (isCorrect) {
            session.setCorrectAnswers(session.getCorrectAnswers() + 1);
            return new AnswerResponseDTO(true, "correctFeedback", answer, null, getSessionStats(sessionId));
        } else {
            int insertionIndex = calculateInsertionIndex(session);
            session.getQuestions().add(insertionIndex, currentQuestion);
            String correctMessage = getCorrectAnswerMessage(currentQuestion);
            return new AnswerResponseDTO(false, "incorrectFeedback", answer, correctMessage, getSessionStats(sessionId));
        }
    }

    private int calculateInsertionIndex(QuizSession session) {
        int offset = 10 + (int)(Math.random() * 6);
        return Math.min(session.getCurrentIndex() + offset, session.getQuestions().size());
    }

    private String getCorrectAnswerMessage(Question question) {
        return question.getOptions().stream()
                .filter(QuestionOption::isCorrect)
                .findFirst()
                .map(opt -> opt.getOptionLabel() + ": " + opt.getOptionText())
                .orElse("Unknown");
    }

    public SessionStatsDTO getSessionStats(String sessionId) {
        QuizSession session = sessions.get(sessionId);
        if (session == null) {
            return new SessionStatsDTO(0, 0, 0.0);
        }
        int correct = session.getCorrectAnswers();
        int answered = session.getAnsweredCount();
        double errorRate = answered == 0 ? 0.0 : ((double)(answered - correct) / answered) * 100.0;
        return new SessionStatsDTO(correct, answered, errorRate);
    }
}
