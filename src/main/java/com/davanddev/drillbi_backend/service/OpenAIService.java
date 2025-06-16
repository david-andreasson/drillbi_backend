package com.davanddev.drillbi_backend.service;

import com.davanddev.drillbi_backend.dto.QuestionDTO;
import com.davanddev.drillbi_backend.dto.QuestionOptionDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class OpenAIService {



    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${anthropic.api.key}")
    private String anthropicApiKey;

    private final PromptService promptService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public OpenAIService(PromptService promptService) {
        this.promptService = promptService;
    }

    private String sendClaudePrompt(String prompt, int maxTokens) {
        String url = "https://api.anthropic.com/v1/messages";
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", anthropicApiKey);
        headers.set("anthropic-version", "2023-06-01");
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "claude-3-haiku-20240307");
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("max_tokens", maxTokens * 2); // Increase max_tokens to get a complete JSON response

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Claude API error: " + response.getStatusCode());
        }

        Map<String, Object> respBody = response.getBody();
        if (respBody == null) {
            throw new RuntimeException("Claude API: Empty response body");
        }

        // Log the entire response in a more readable format
        log.info("Claude API response:");
        log.info("Response class: {}", respBody.getClass());
        log.info("Response keys: {}", respBody.keySet());
        log.info("Response toString: {}", respBody.toString());
        
        // Log each key and its value
        respBody.forEach((key, value) -> {
            log.info("Key: {} - Value: {}", key, value);
        });
        
        // Log content array in more detail
        List<Map<String, Object>> contentArray = objectMapper.convertValue(respBody.get("content"), new TypeReference<List<Map<String, Object>>>() {});
        if (contentArray != null) {
            log.info("Content array contains {} elements", contentArray.size());
            for (int i = 0; i < contentArray.size(); i++) {
                Map<String, Object> item = contentArray.get(i);
                log.info("Content item {}:", i);
                item.forEach((k, v) -> {
                    log.info("  {} = {}", k, v);
                });
            }
        }

        // Get content array and extract the text
        List<Map<String, Object>> content = objectMapper.convertValue(respBody.get("content"), new TypeReference<List<Map<String, Object>>>() {});
        if (content == null || content.isEmpty()) {
            throw new RuntimeException("Claude API: No content in response");
        }

        Map<String, Object> textContent = content.get(0);
        if (!textContent.containsKey("text")) {
            throw new RuntimeException("Claude API: No text in content");
        }

        return (String) textContent.get("text");
    }

    public String generateExplanationWithXmlPrompt(
            QuestionDTO question,
            String selectedOption,
            String language,
            String sourceText,
            String aiModel
    ) {
        String correct = question.getOptions().stream()
                .filter(QuestionOptionDTO::isCorrect)
                .findFirst()
                .map(QuestionOptionDTO::getOptionText)
                .orElse("");

        String promptId, prompt;
        if (sourceText != null && !sourceText.isBlank()) {
            promptId = "ai-explanation-strict";
            prompt = promptService.getPromptById(
                    promptId, language, sourceText, correct, question.getQuestionText()
            );
        } else {
            promptId = "ai-explanation";
            prompt = promptService.getPromptById(
                    promptId, language, question.getCourseName(), correct, question.getQuestionText()
            );
        }
        if ("claude".equalsIgnoreCase(aiModel)) {
            return sendClaudePrompt("You are a helpful teacher. Explain:\n" + prompt, 400);
        }
        return callOpenAI(List.of(Map.of("role", "user", "content", prompt)), "gpt-4o", 400);
    }

    private String callOpenAI(List<Map<String, Object>> messages, String model, int maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> req = new HashMap<>();
        req.put("model", model);
        req.put("temperature", 0.7);
        req.put("max_tokens", maxTokens);
        req.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(req, headers);
        ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                "https://api.openai.com/v1/chat/completions",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        Map<String, Object> body = resp.getBody();
        if (body == null) {
            throw new RuntimeException("No response body from OpenAI");
        }
        List<Map<String, Object>> choices = objectMapper.convertValue(body.get("choices"), new TypeReference<List<Map<String, Object>>>() {});
        if (choices == null || choices.isEmpty()) {
            throw new RuntimeException("No choices in OpenAI response");
        }
        Map<String, Object> msg = objectMapper.convertValue(choices.get(0).get("message"), new TypeReference<Map<String, Object>>() {});
        return (String) msg.get("content");
    }

    public List<QuestionDTO> generateQuestions(
            String text, String courseName, String language, int maxQuestions, String aiModel
    ) {
        validateInputs(text, maxQuestions);
        String systemPrompt = promptService.getPromptOrThrow("system", language);
        String courseForPrompt = (courseName == null || courseName.isBlank()) ? "UNKNOWN" : courseName;
        String userPrompt = promptService.getPromptById(
                "generate-questions", language, maxQuestions, courseForPrompt, text, courseForPrompt, courseForPrompt
        );
        String content;
        if ("claude".equalsIgnoreCase(aiModel)) {
            content = sendClaudePrompt(systemPrompt + "\n" + userPrompt, 2000);
        } else {
            content = callOpenAI(
                    List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "gpt-4",
                    2000
            );
        }
        return parseQuestions(content);
    }

    public List<QuestionDTO> regenerateQuestion(
            String sourceText, String courseName, String language,
            String originalQuestion, String aiModel
    ) {
        String systemPrompt = promptService.getPromptOrThrow("system", language);
        String userPrompt = promptService.getPromptById(
                "regenerate-question", language,
                courseName, sourceText, originalQuestion, courseName, courseName
        );
        String content = "claude".equalsIgnoreCase(aiModel)
                ? sendClaudePrompt(systemPrompt + "\n" + userPrompt, 1024)
                : callOpenAI(
                List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "gpt-4",
                1024
        );
        return parseQuestions(content);
    }

    public List<QuestionOptionDTO> generateOptionsFromAI(
            String questionText, String language, String courseName,
            String sourceText, String aiModel
    ) {
        String systemPrompt = promptService.getPromptOrThrow("system", language);
        String userPrompt = promptService.getPromptById(
                "regenerate-options", language, courseName, sourceText, questionText, courseName
        );
        String content = "claude".equalsIgnoreCase(aiModel)
                ? sendClaudePrompt(systemPrompt + "\n" + userPrompt, 512)
                : callOpenAI(
                List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "gpt-4",
                512
        );
        return parseOptions(content);
    }
    private void validateInputs(String text, int maxQuestions) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text may not be empty");
        }
        if (maxQuestions <= 0 || maxQuestions > 50) {
            throw new IllegalArgumentException("maxQuestions must be between 1 and 50");
        }
    }

    private List<QuestionDTO> parseQuestions(String content) {
        String json = content;
        if (json == null || json.isEmpty()) {
            throw new RuntimeException("Empty response from AI");
        }
        try {
            // Försök att parsa direkt som en lista av DTO
            try {
                List<QuestionDTO> direct = objectMapper.readValue(json, new TypeReference<List<QuestionDTO>>() {});
                log.debug("Parsed questions directly as List<QuestionDTO>: {}", direct);
                return direct;
            } catch (Exception e) {
                log.debug("Direct array parse failed, trying fallback. Error: {}", e.getMessage());
            }
            // Försök som objekt med content-fält
            try {
                Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
                if (map.containsKey("content")) {
                    Object contentObj = map.get("content");
                    if (contentObj instanceof List<?>) {
                        String arrJson = objectMapper.writeValueAsString(contentObj);
                        List<QuestionDTO> arr = objectMapper.readValue(arrJson, new TypeReference<List<QuestionDTO>>() {});
                        log.debug("Parsed questions from wrapped 'content' array: {}", arr);
                        return arr;
                    } else if (contentObj instanceof String) {
                        json = (String) contentObj;
                    }
                }
            } catch (Exception e) {
                log.debug("Wrapped object parse failed, trying fallback. Error: {}", e.getMessage());
            }
            // Rensa whitespace och försök igen
            json = json.replaceAll("\\n", " ");
            json = json.replaceAll("\\r", " ");
            json = json.replaceAll("\\t", " ");
            log.debug("Final JSON string to parse (questions): {}", json);
            List<QuestionDTO> list = objectMapper.readValue(json, new TypeReference<List<QuestionDTO>>() {});
            return list;
        } catch (Exception e) {
            if (json != null) {
                log.error("Failed to parse JSON: {}", json);
            }
            log.error("Error details:", e);
            throw new RuntimeException("Failed to parse AI response (questions): " + e.getMessage(), e);
        }
    }

    private List<QuestionOptionDTO> parseOptions(String content) {
        String json = content;
        if (json == null || json.isEmpty()) {
            throw new RuntimeException("Empty response from AI");
        }
        try {
            // Försök att parsa direkt som en lista av DTO
            try {
                List<QuestionOptionDTO> direct = objectMapper.readValue(json, new TypeReference<List<QuestionOptionDTO>>() {});
                log.debug("Parsed options directly as List<QuestionOptionDTO>: {}", direct);
                return direct;
            } catch (Exception e) {
                log.debug("Direct array parse failed, trying fallback. Error: {}", e.getMessage());
            }
            // Försök som objekt med content-fält
            try {
                Map<String, Object> map = objectMapper.readValue(json, new TypeReference<>() {});
                if (map.containsKey("content")) {
                    Object contentObj = map.get("content");
                    if (contentObj instanceof List<?>) {
                        String arrJson = objectMapper.writeValueAsString(contentObj);
                        List<QuestionOptionDTO> arr = objectMapper.readValue(arrJson, new TypeReference<List<QuestionOptionDTO>>() {});
                        log.debug("Parsed options from wrapped 'content' array: {}", arr);
                        return arr;
                    } else if (contentObj instanceof String) {
                        json = (String) contentObj;
                    }
                }
            } catch (Exception e) {
                log.debug("Wrapped object parse failed, trying fallback. Error: {}", e.getMessage());
            }
            // Rensa whitespace och försök igen
            json = json.replaceAll("\\n", " ");
            json = json.replaceAll("\\r", " ");
            json = json.replaceAll("\\t", " ");
            log.debug("Final JSON string to parse (options): {}", json);
            List<QuestionOptionDTO> list = objectMapper.readValue(json, new TypeReference<List<QuestionOptionDTO>>() {});
            return list;
        } catch (Exception e) {
            if (json != null) {
                log.error("Failed to parse JSON: {}", json);
            }
            log.error("Error details:", e);
            throw new RuntimeException("Failed to parse AI response (options): " + e.getMessage(), e);
        }
    }
}