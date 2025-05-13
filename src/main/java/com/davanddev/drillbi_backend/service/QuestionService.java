package com.davanddev.drillbi_backend.service;

import com.davanddev.drillbi_backend.models.Question;

import java.util.List;

public interface QuestionService {

    List<Question> getQuestions(String courseName, String orderType);

    long getQuestionCount(String courseName);

    void saveQuestions(List<Question> questions, String userGroup);

    java.util.List<com.davanddev.drillbi_backend.dto.QuestionOptionDTO> generateOptionsFromAI(String questionText, String language, String courseName);
}