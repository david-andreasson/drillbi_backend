package com.davanddev.drillbi_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AnswerResponseDTO {
    private boolean correct;
    private String feedbackKey;
    private String option;
    private String correctAnswer;
    private SessionStatsDTO stats;

    public AnswerResponseDTO() {}

    public AnswerResponseDTO(boolean correct, String feedbackKey, String option, String correctAnswer, SessionStatsDTO stats) {
        this.correct = correct;
        this.feedbackKey = feedbackKey;
        this.option = option;
        this.correctAnswer = correctAnswer;
        this.stats = stats;
    }

}
