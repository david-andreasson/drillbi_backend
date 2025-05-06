package com.davanddev.quizapp_api.session;

import com.davanddev.quizapp_api.models.Question;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.UUID;

/**
 * Represents a quiz session that tracks the questions, current index, and statistics.
 */
@Getter
@Setter
@ToString
public class QuizSession {

    private final String sessionId;
    private final String courseName;
    private final String orderType;
    private final List<Question> questions;

    private int currentIndex;
    private int answeredCount;
    private int correctAnswers;

    /**
     * Constructs a new QuizSession with the given course name, order type, and list of questions.
     *
     * @param courseName the course name
     * @param orderType  the order type (e.g., "ORDER", "RANDOM", "REVERSE")
     * @param questions  the list of questions
     */
    public QuizSession(String courseName, String orderType, List<Question> questions) {
        this.sessionId = UUID.randomUUID().toString();
        this.courseName = courseName;
        this.orderType = orderType;
        this.questions = questions;
        this.currentIndex = 0;
        this.answeredCount = 0;
        this.correctAnswers = 0;
    }

    /**
     * Returns the current number of questions in the session.
     * This value is dynamic and reflects any reinsertion of questions.
     *
     * @return the number of questions
     */
    public int getTotalQuestions() {
        return questions.size();
    }
}
