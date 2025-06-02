package com.davanddev.drillbi_backend.controller;

import com.davanddev.drillbi_backend.dto.QuestionDTO;
import com.davanddev.drillbi_backend.dto.QuestionOptionDTO;
import com.davanddev.drillbi_backend.models.QuestionOption;
import com.davanddev.drillbi_backend.models.Course;
import com.davanddev.drillbi_backend.repository.QuestionRepository;
import com.davanddev.drillbi_backend.repository.CourseRepository;
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

    @PostMapping("/create")
    public ResponseEntity<?> createQuestion(
            @RequestParam("questionText") String questionText,
            @RequestParam("options") String optionsJson,
            @RequestParam("correctIndex") String correctIndexStr,
            @RequestParam(value = "courseName", required = false) String courseName,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        logger.info("POST /api/v2/questions/create - questionText={}, optionsJson={}, correctIndexStr={}, courseName={}, imageFilePresent={}", questionText, optionsJson, correctIndexStr, courseName, imageFile != null);

        try {
            // Parsea optionsJson till lista
            ObjectMapper mapper = new ObjectMapper();
            List<QuestionOptionDTO> optionDTOs = Arrays.asList(mapper.readValue(optionsJson, QuestionOptionDTO[].class));
            int correctIndex = Integer.parseInt(correctIndexStr);

            // Validera obligatoriska fält
            if (questionText == null || questionText.isBlank()) {
                logger.error("createQuestion: Saknar frågetext");
                return ResponseEntity.badRequest().body("error.questionTextRequired");
            }
            if (optionsJson == null || optionsJson.isBlank()) {
                logger.error("createQuestion: Saknar optionsJson");
                return ResponseEntity.badRequest().body("error.optionsRequired");
            }
            if (correctIndexStr == null || correctIndexStr.isBlank()) {
                logger.error("createQuestion: Saknar eller ogiltigt correctIndexStr");
                return ResponseEntity.badRequest().body("error.correctIndexInvalid");
            }
            if (courseName == null || courseName.isBlank()) {
                logger.error("createQuestion: Saknar kursnamn");
                return ResponseEntity.badRequest().body("error.courseNameRequired");
            }
            // Hämta eller skapa kurs
            Course course = courseRepository.findByName(courseName).orElse(null);
            if (course == null) {
                course = new Course();
                course.setName(courseName);
                course.setDisplayName(courseName);
                course.setDescription("");
                course.setUserGroup("UNKNOWN");
                course = courseRepository.save(course);
            }

            // Spara bild om den finns
            String imageUrl = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String uploadsDir = System.getProperty("user.dir") + File.separator + "images";
                    File dir = new File(uploadsDir);
                    if (!dir.exists()) dir.mkdirs();
                    String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                    File dest = new File(dir, filename);
                    imageFile.transferTo(dest);
                    imageUrl = "/images/" + filename;
                } catch (Exception e) {
                    logger.error("createQuestion: Kunde inte ladda upp bilden", e);
                    return ResponseEntity.badRequest().body("error.imageUploadFailed");
                }
            }

            // Skapa Question och QuestionOption
            Question question = new Question();
            question.setQuestionText(questionText);
            question.setCourse(course);
            question.setQuestionNumber(0); // TODO: sätt rätt nummer
            question.setImageUrl(imageUrl);
            List<QuestionOption> options = new ArrayList<>();
            for (int i = 0; i < optionDTOs.size(); i++) {
                QuestionOptionDTO dto = optionDTOs.get(i);
                QuestionOption opt = new QuestionOption();
                opt.setOptionLabel(dto.getOptionLabel());
                opt.setOptionText(dto.getOptionText());
                opt.setCorrect(i == correctIndex);
                opt.setQuestion(question);
                options.add(opt);
            }
            question.setOptions(options);

            // Spara frågan
            Question saved = questionRepository.save(question);
            QuestionDTO dto = DtoMapper.toQuestionDTO(saved);
            logger.info("createQuestion: Fråga skapad OK för kurs {}", courseName);
            return ResponseEntity.status(201).body(dto);
        } catch (Exception e) {
            logger.error("createQuestion: Exception/fel", e);
            return ResponseEntity.internalServerError().body("error.questionCreateFailed");
        }
    }


    private final QuestionService questionService;
    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;

    public QuestionController(QuestionService questionService, QuestionRepository questionRepository, CourseRepository courseRepository) {
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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuestionDTO> updateQuestion(
            @PathVariable Long id,
            @RequestParam("questionText") String questionText,
            @RequestParam("options") String optionsJson,
            @RequestParam("correctIndex") Integer correctIndex,
            @RequestParam(value = "image", required = false) MultipartFile imageFile
    ) {
        Optional<Question> optQuestion = questionRepository.findById(id);
        if (optQuestion.isEmpty()) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.NOT_FOUND).build();
        }
        Question question = optQuestion.get();
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<QuestionOptionDTO> optionDTOs = Arrays.asList(mapper.readValue(optionsJson, QuestionOptionDTO[].class));
            // Update question fields
            question.setQuestionText(questionText);
            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String uploadsDir = System.getProperty("user.dir") + File.separator + "images";
                File dir = new File(uploadsDir);
                if (!dir.exists()) dir.mkdirs();
                String filename = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                File dest = new File(dir, filename);
                imageFile.transferTo(dest);
                question.setImageUrl("/images/" + filename);
            }
            // Update options
            List<QuestionOption> options = new ArrayList<>();
            for (int i = 0; i < optionDTOs.size(); i++) {
                QuestionOptionDTO dto = optionDTOs.get(i);
                QuestionOption opt = new QuestionOption();
                opt.setOptionLabel(dto.getOptionLabel());
                opt.setOptionText(dto.getOptionText());
                opt.setCorrect(i == correctIndex);
                opt.setQuestion(question);
                options.add(opt);
            }
            question.setOptions(options);
            Question saved = questionRepository.save(question);
            return ResponseEntity.ok(DtoMapper.toQuestionDTO(saved));
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}