package com.davanddev.quizapp_api.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class QuestionDTO {
    private Long id;
    private int questionNumber;
    private String courseName;
    private String courseDisplayName;
    private String courseDescription;
    private String questionText;
    private List<QuestionOptionDTO> options;

    public QuestionDTO() {}

    public QuestionDTO(Long id, int questionNumber, String courseName, String courseDisplayName, String questionText, List<QuestionOptionDTO> options) {
        this.id = id;
        this.questionNumber = questionNumber;
        this.courseName = courseName;
        this.courseDisplayName = courseDisplayName;
        this.questionText = questionText;
        this.options = options;
    }
}