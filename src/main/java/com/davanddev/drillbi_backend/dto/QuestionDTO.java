package com.davanddev.drillbi_backend.dto;

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
    private String imageUrl;

    public QuestionDTO() {}

    public QuestionDTO(Long id, int questionNumber, String courseName, String courseDisplayName, String questionText, List<QuestionOptionDTO> options, String imageUrl) {
        this.id = id;
        this.questionNumber = questionNumber;
        this.courseName = courseName;
        this.courseDisplayName = courseDisplayName;
        this.questionText = questionText;
        this.options = options;
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}