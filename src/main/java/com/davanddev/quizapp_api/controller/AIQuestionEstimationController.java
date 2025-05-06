//package com.davanddev.quizapp_api.controller;
//
//import com.davanddev.quizapp_api.service.OpenAIService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/v2/ai")
//public class AIQuestionEstimationController {
//    private final OpenAIService openAIService;
//
//    @Autowired
//    public AIQuestionEstimationController(OpenAIService openAIService) {
//        this.openAIService = openAIService;
//    }
//
//    /**
//     * Endpoint to estimate the number of unique, meaningful quiz questions that can be generated from a given text.
//     * Expects a JSON body with 'text' and optional 'language' (defaults to 'sv').
//     */
//    @PostMapping("/estimate-question-potential")
//    public ResponseEntity<Integer> estimateQuestionPotential(@RequestBody Map<String, String> request) {
//    System.out.println("[AIQuestionEstimationController] Received request: " + request);
//    String text = request.get("text");
//String language = request.getOrDefault("language", "sv");
//String aiModel = request.getOrDefault("aiModel", "openai");
//System.out.println("[AIQuestionEstimationController] text length: " + (text == null ? 0 : text.length()));
//System.out.println("[AIQuestionEstimationController] language: " + language);
//System.out.println("[AIQuestionEstimationController] aiModel: " + aiModel);
//if (text == null || text.isBlank()) {
//    return ResponseEntity.badRequest().build();
//}
//int estimated = openAIService.estimateQuestionPotential(text, language, aiModel);
//return ResponseEntity.ok(estimated);
//    }
//}
