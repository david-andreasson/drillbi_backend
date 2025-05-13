package com.davanddev.drillbi_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class GenerateQuestionsRequest {
    private Integer maxQuestions;
    private String originalQuestion;
    private String text;
    private String courseName;
    private String language;

    @JsonAlias({ "ai", "aiModel" })
    private String aiModel;
}