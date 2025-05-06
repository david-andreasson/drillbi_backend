package com.davanddev.quizapp_api.controller;

import com.davanddev.quizapp_api.dto.GenerateQuestionsRequest;
import com.davanddev.quizapp_api.dto.QuestionDTO;
import com.davanddev.quizapp_api.dto.RegenerateOptionsRequest;
import com.davanddev.quizapp_api.dto.QuestionOptionDTO;
import com.davanddev.quizapp_api.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v2")
public class GenerateQuestionsController {

    private final OpenAIService openAIService;

    @Autowired
    public GenerateQuestionsController(OpenAIService openAIService) {
        this.openAIService = openAIService;
    }

    @PostMapping("/generate-questions")
    public List<QuestionDTO> generateQuestions(@RequestBody GenerateQuestionsRequest request) {
        String aiModel = request.getAiModel() != null && !request.getAiModel().isBlank()
                ? request.getAiModel()
                : "openai";

        // If originalQuestion exists, use the regenerate-question prompt
        if (request.getOriginalQuestion() != null && !request.getOriginalQuestion().isBlank()) {
            return openAIService.regenerateQuestion(
                    request.getText(),
                    request.getCourseName(),
                    request.getLanguage(),
                    request.getOriginalQuestion(),
                    aiModel
            );
        }

        // Otherwise, use standard generation with support for maxQuestions
        int maxQuestions = (request.getMaxQuestions() != null && request.getMaxQuestions() > 0)
                ? request.getMaxQuestions()
                : 5;

        return openAIService.generateQuestions(
                request.getText(),
                request.getCourseName(),
                request.getLanguage(),
                maxQuestions,
                aiModel
        );
    }

    @PostMapping("/questions/regenerate-options")
    public List<QuestionOptionDTO> regenerateOptions(@RequestBody RegenerateOptionsRequest request) {
        String aiModel = request.getAiModel() != null && !request.getAiModel().isBlank()
                ? request.getAiModel()
                : "openai";

        return openAIService.generateOptionsFromAI(
                request.getQuestionText(),
                request.getLanguage(),
                request.getCourseName(),
                request.getSourceText(),
                aiModel
        );
    }
}