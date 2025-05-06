package com.davanddev.quizapp_api.service;

import com.davanddev.quizapp_api.models.Question;

import java.util.List;

public interface QuestionService {

    List<Question> getQuestions(String courseName, String orderType);

    long getQuestionCount(String courseName);

    void saveQuestions(List<Question> questions, String userGroup);

    java.util.List<com.davanddev.quizapp_api.dto.QuestionOptionDTO> generateOptionsFromAI(String questionText, String language, String courseName);
}