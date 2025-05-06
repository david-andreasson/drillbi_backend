package com.davanddev.quizapp_api.service;

import com.davanddev.quizapp_api.models.Course;
import com.davanddev.quizapp_api.models.Question;
import com.davanddev.quizapp_api.models.QuestionOption;
import com.davanddev.quizapp_api.repository.CourseRepository;
import com.davanddev.quizapp_api.repository.QuestionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final CourseRepository courseRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository, CourseRepository courseRepository) {
        this.questionRepository = questionRepository;
        this.courseRepository = courseRepository;
    }

    @Override
    public List<Question> getQuestions(String courseName, String orderType) {
        List<Question> questions = questionRepository.findByCourse_Name(courseName);
        questions.sort(Comparator.comparingInt(Question::getQuestionNumber));

        if ("RANDOM".equalsIgnoreCase(orderType)) {
            Collections.shuffle(questions);
        } else if ("REVERSE".equalsIgnoreCase(orderType)) {
            Collections.reverse(questions);
        }

        return questions;
    }

    @Override
    public long getQuestionCount(String courseName) {
        return questionRepository.countByCourse_Name(courseName);
    }

    @Override
    public void saveQuestions(List<Question> questions, String userGroup) {
        if (questions == null || questions.isEmpty()) {
            log.warn("No questions to save.");
            return;
        }

        // Resolve a valid course name (ignore UNKNOWN placeholders)
        String resolvedCourseName = questions.stream()
                .map(q -> q.getCourse().getName())
                .filter(name -> !"UNKNOWN".equalsIgnoreCase(name))
                .findFirst()
                .orElse("UNKNOWN");

        // Group by (possibly normalized) course name
        Map<String, List<Question>> groupedByCourse = questions.stream()
                .collect(Collectors.groupingBy(q -> {
                    String name = q.getCourse().getName();
                    return "UNKNOWN".equalsIgnoreCase(name) ? resolvedCourseName : name;
                }));

        // Process each course group
        for (Map.Entry<String, List<Question>> entry : groupedByCourse.entrySet()) {
            String courseName = entry.getKey();
            List<Question> courseQuestions = entry.getValue();

            // Extract displayName and description from first question's Course
            String displayName = courseQuestions.get(0).getCourse().getDisplayName();
            String description = courseQuestions.get(0).getCourse().getDescription();

            // Find or create Course entity
            Course course = courseRepository.findByName(courseName)
                    .orElseGet(() -> {
                        Course newCourse = new Course();
                        newCourse.setName(courseName);
                        newCourse.setDisplayName(displayName);
                        newCourse.setDescription(description);
                        newCourse.setUserGroup(userGroup);
                        return courseRepository.save(newCourse);
                    });

            // Update existing Course if missing fields
            boolean updated = false;
            if (course.getDisplayName() == null || course.getDisplayName().isBlank()) {
                course.setDisplayName(displayName);
                updated = true;
            }
            if (course.getDescription() == null || course.getDescription().isBlank()) {
                course.setDescription(description);
                updated = true;
            }
            if (updated) {
                course = courseRepository.save(course);
            }

            // Determine offset for numbering
            int questionOffset = (int) questionRepository.countByCourse_Name(courseName);

            // Assign course and number to each Question and fix options
            for (int i = 0; i < courseQuestions.size(); i++) {
                Question q = courseQuestions.get(i);
                q.setCourse(course);
                q.setQuestionNumber(questionOffset + i + 1);

                if (q.getOptions() != null) {
                    for (QuestionOption option : q.getOptions()) {
                        option.setQuestion(q);
                    }
                }
            }
        }

        // Save all questions (cascade will persist options)
        questionRepository.saveAll(questions);

        log.info("Saved {} questions", questions.size());
        questions.forEach(q ->
                log.info("→ [{} - #{}] {}", q.getCourse().getName(), q.getQuestionNumber(), q.getQuestionText())
        );
    }

    @Override
    public java.util.List<com.davanddev.quizapp_api.dto.QuestionOptionDTO> generateOptionsFromAI(String questionText, String language, String courseName) {
        return java.util.Collections.emptyList();
    }
}
