package com.davanddev.drillbi_backend.controller;

import com.davanddev.drillbi_backend.dto.QuestionDTO;
import com.davanddev.drillbi_backend.dto.QuestionOptionDTO;
import com.davanddev.drillbi_backend.models.QuestionOption;

import com.davanddev.drillbi_backend.repository.QuestionRepository;
import com.davanddev.drillbi_backend.models.Question;
import com.davanddev.drillbi_backend.service.QuestionService;
import com.davanddev.drillbi_backend.util.DtoMapper;

import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.io.File;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/v2/questions")
public class QuestionController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(QuestionController.class);

    private final QuestionService questionService;
    private final QuestionRepository questionRepository;
    private final com.davanddev.drillbi_backend.repository.CourseRepository courseRepository;

    public QuestionController(QuestionService questionService, QuestionRepository questionRepository, com.davanddev.drillbi_backend.repository.CourseRepository courseRepository) {
        this.questionService = questionService;
        this.questionRepository = questionRepository;
        this.courseRepository = courseRepository;
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

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getQuestion(@PathVariable Long id) {
        return questionRepository.findById(id)
            .map(DtoMapper::toQuestionDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // Uppdatera fråga
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @RequestParam("questionText") String questionText,
            @RequestParam("options") String optionsJson,
            @RequestParam("correctIndex") Integer correctIndex,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        logger.info("[updateQuestion] Called for id={} text='{}' correctIndex={} imageFileNull={}", id, questionText, correctIndex, imageFile == null);
        logger.info("[updateQuestion] optionsJson: {}", optionsJson);
        Optional<Question> optQuestion = questionRepository.findById(id);
        if (optQuestion.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).build();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<QuestionOptionDTO> optionDTOs = Arrays.asList(mapper.readValue(optionsJson, QuestionOptionDTO[].class));
            if (optionDTOs.size() != 4) {
                logger.error("updateQuestion: Fel antal alternativ ({}), måste vara exakt 4", optionDTOs.size());
                return ResponseEntity.badRequest().body(null);
            }
            Question question = optQuestion.get();
            question.setQuestionText(questionText);
            if (imageFile != null && !imageFile.isEmpty()) {
                String uploadsDir = System.getProperty("user.dir") + File.separator + "images";
                File dir = new File(uploadsDir);
                if (!dir.exists()) dir.mkdirs();
                String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                File dest = new File(dir, filename);
                imageFile.transferTo(dest);
                question.setImageUrl("/images/" + filename);
            }
            // Behåll gammalt frågenummer
            // Alternativ
            // Byt ut alternativen på rätt sätt för Hibernate orphan-removal
            List<QuestionOption> newOptions = new ArrayList<>();
            for (int i = 0; i < optionDTOs.size(); i++) {
                QuestionOptionDTO dto = optionDTOs.get(i);
                QuestionOption opt = new QuestionOption();
                opt.setOptionLabel(dto.getOptionLabel());
                opt.setOptionText(dto.getOptionText());
                opt.setCorrect(i == correctIndex);
                opt.setQuestion(question);
                newOptions.add(opt);
            }
            // Töm gamla alternativ och lägg till nya
            question.getOptions().clear();
            question.getOptions().addAll(newOptions);
            Question saved = questionRepository.save(question);
            return ResponseEntity.ok(DtoMapper.toQuestionDTO(saved));
        } catch (Exception e) {
            logger.error("[updateQuestion] FEL: {}\nquestionText: {}\noptionsJson: {}\ncorrectIndex: {}\nimageFileNull: {}", e.getMessage(), questionText, optionsJson, correctIndex, imageFile == null, e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Skapa ny fråga
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionDTO> createQuestion(
            @RequestParam("questionText") String questionText,
            @RequestParam("options") String optionsJson,
            @RequestParam("correctIndex") Integer correctIndex,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "courseName", required = false) String courseName
    ) {
        logger.info("[createQuestion] Called for text='{}' correctIndex={} imageFileNull={}", questionText, correctIndex, imageFile == null);
        logger.info("[createQuestion] optionsJson: {}", optionsJson);
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<QuestionOptionDTO> optionDTOs = Arrays.asList(mapper.readValue(optionsJson, QuestionOptionDTO[].class));
            if (optionDTOs.size() != 4) {
                logger.error("createQuestion: Fel antal alternativ ({}), måste vara exakt 4", optionDTOs.size());
                return ResponseEntity.badRequest().body(null);
            }
            Question question = new Question();
            question.setQuestionText(questionText);
            if (courseName != null) {
                Optional<com.davanddev.drillbi_backend.models.Course> courseOpt = courseRepository.findByName(courseName);
                if (courseOpt.isEmpty()) {
                    logger.error("createQuestion: Kunde inte hitta kurs med namn {}", courseName);
                    return ResponseEntity.badRequest().body(null);
                }
                question.setCourse(courseOpt.get());
            }
            if (imageFile != null && !imageFile.isEmpty()) {
                String uploadsDir = System.getProperty("user.dir") + File.separator + "images";
                File dir = new File(uploadsDir);
                if (!dir.exists()) dir.mkdirs();
                String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                File dest = new File(dir, filename);
                imageFile.transferTo(dest);
                question.setImageUrl("/images/" + filename);
            }
            // Alternativ
            List<QuestionOption> newOptions = new ArrayList<>();
            for (int i = 0; i < optionDTOs.size(); i++) {
                QuestionOptionDTO dto = optionDTOs.get(i);
                QuestionOption opt = new QuestionOption();
                opt.setOptionLabel(dto.getOptionLabel());
                opt.setOptionText(dto.getOptionText());
                opt.setCorrect(i == correctIndex);
                opt.setQuestion(question);
                newOptions.add(opt);
            }
            question.setOptions(newOptions);
            Question saved = questionRepository.save(question);
            return ResponseEntity.ok(DtoMapper.toQuestionDTO(saved));
        } catch (Exception e) {
            logger.error("[createQuestion] FEL: {}\nquestionText: {}\noptionsJson: {}\ncorrectIndex: {}\nimageFileNull: {}", e.getMessage(), questionText, optionsJson, correctIndex, imageFile == null, e);
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}