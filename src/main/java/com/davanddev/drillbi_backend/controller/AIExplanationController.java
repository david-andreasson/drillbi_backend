package com.davanddev.drillbi_backend.controller;

import com.davanddev.drillbi_backend.dto.QuestionDTO;
import com.davanddev.drillbi_backend.service.DailyAIUsageService;
import com.davanddev.drillbi_backend.service.OpenAIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v2")
public class AIExplanationController {

    private final OpenAIService openAIService;
    private final DailyAIUsageService dailyAIUsageService;

    @Autowired
    public AIExplanationController(OpenAIService openAIService, DailyAIUsageService dailyAIUsageService) {
        this.openAIService = openAIService;
        this.dailyAIUsageService = dailyAIUsageService;
    }

    @PostMapping("/explain")
    public ResponseEntity<String> explainWithAI(
            @RequestBody @NotNull Map<String, Object> request,
            @AuthenticationPrincipal Jwt principal
    ) {
        Object questionObj = request.get("question");
        String selectedOption = (String) request.get("selectedOption");
        String lang = (String) request.getOrDefault("language", "sv");

        if (questionObj == null || selectedOption == null) {
            return ResponseEntity.badRequest().body("Missing required fields.");
        }

        QuestionDTO question = new ObjectMapper().convertValue(questionObj, QuestionDTO.class);

        String username = (principal != null) ? principal.getSubject() : "devUser";
        if (dailyAIUsageService.hasExceededDailyLimit(username)) {
            String msg = lang.equalsIgnoreCase("en")
                    ? "You have reached your free daily limit for AI requests. Please try again tomorrow."
                    : "Du har uppnått din gräns för gratis AI-anrop för dagen. Imorgon får du 5 nya anrop.";
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(msg);
        }

        String sourceText = (String) request.getOrDefault("sourceText", "");
        String aiModel = (String) request.getOrDefault("aiModel", "openai");
        try {
            String explanation = openAIService.generateExplanationWithXmlPrompt(question, selectedOption, lang, sourceText, aiModel);
            dailyAIUsageService.incrementUsage(username);
            return ResponseEntity.ok(explanation.trim());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to get AI explanation: " + e.getMessage());
        }
    }

}