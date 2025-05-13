package com.davanddev.drillbi_backend.controller;

import com.davanddev.drillbi_backend.dto.QuestionDTO;
import com.davanddev.drillbi_backend.models.Question;
import com.davanddev.drillbi_backend.service.QuestionService;
import com.davanddev.drillbi_backend.util.DtoMapper;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
        }


    // Endpoint to retrieve questions for a given course with a specified sort order.
    @GetMapping
    public List<QuestionDTO> getQuestions(@RequestParam String courseName,
                                          @RequestParam(required = false, defaultValue = "ORDER") String orderType) {
        List<Question> questions = questionService.getQuestions(courseName, orderType);
        return questions.stream()
                .map(DtoMapper::toQuestionDTO)
                .collect(Collectors.toList());
        }


    // Endpoint to retrieve the question count for a course
    @GetMapping("/count")
    public long getQuestionCount(@RequestParam String courseName) {
        return questionService.getQuestionCount(courseName);
        }


    // Endpoint to save a list of questions — now uses userGroup from JWT
    @PostMapping("/batch")
    public void saveQuestionsBatch(@RequestBody @NotNull List<QuestionDTO> questionDTOs,
                                   @AuthenticationPrincipal Jwt principal) {

        List<Question> questions = questionDTOs.stream()
                .map(DtoMapper::toQuestion)
                .collect(Collectors.toList());

        // Default to "UNKNOWN" if userGroup claim doesn't exist (fail-safe)
        String userGroup = principal.getClaimAsString("userGroup");
        if (userGroup == null || userGroup.isBlank()) {
            userGroup = "UNKNOWN";
            }


        questionService.saveQuestions(questions, userGroup);
        }

    }