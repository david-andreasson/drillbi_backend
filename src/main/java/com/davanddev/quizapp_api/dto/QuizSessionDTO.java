package com.davanddev.quizapp_api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizSessionDTO {
    private String sessionId;
    private List<QuestionDTO> questions;
    private int currentIndex;
    private int answeredCount;
    private int correctAnswers;
}

