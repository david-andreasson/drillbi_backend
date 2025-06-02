package com.davanddev.drillbi_backend.util;

import com.davanddev.drillbi_backend.dto.CourseDTO;
import com.davanddev.drillbi_backend.dto.QuestionDTO;
import com.davanddev.drillbi_backend.dto.QuestionOptionDTO;
import com.davanddev.drillbi_backend.dto.QuizSessionDTO;
import com.davanddev.drillbi_backend.models.Course;
import com.davanddev.drillbi_backend.models.Question;
import com.davanddev.drillbi_backend.models.QuestionOption;
import com.davanddev.drillbi_backend.session.QuizSession;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper utility to convert between Entity and DTO classes.
 */
public class DtoMapper {

    public static CourseDTO toCourseDTO(Course course) {
        CourseDTO dto = new CourseDTO();
        dto.setId(course.getId());
        dto.setName(course.getName());
        dto.setDisplayName(course.getDisplayName());
        dto.setDescription(course.getDescription());
        return dto;
    }

    public static QuestionDTO toQuestionDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionNumber(question.getQuestionNumber());
        dto.setCourseName(question.getCourse().getName());
        dto.setCourseDisplayName(question.getCourse().getDisplayName());
        dto.setCourseDescription(question.getCourse().getDescription());
        dto.setQuestionText(question.getQuestionText());
        dto.setOptions(
                question.getOptions().stream()
                        .map(DtoMapper::toOptionDTO)
                        .collect(Collectors.toList())
        );
        dto.setImageUrl(question.getImageUrl());
        return dto;
    }

    public static Question toQuestion(QuestionDTO dto) {
        Question question = new Question();

        // Ensure Course entity carries both displayName and description
        Course course = new Course();
        course.setName(dto.getCourseName());
        course.setDisplayName(dto.getCourseDisplayName());
        course.setDescription(dto.getCourseDescription());
        question.setCourse(course);

        question.setQuestionNumber(dto.getQuestionNumber());
        question.setQuestionText(dto.getQuestionText());

        // Map options
        List<QuestionOption> opts = dto.getOptions().stream()
                .map(o -> {
                    QuestionOption opt = new QuestionOption();
                    opt.setOptionLabel(o.getOptionLabel());
                    opt.setOptionText(o.getOptionText());
                    opt.setCorrect(o.isCorrect());
                    opt.setQuestion(question);
                    return opt;
                })
                .collect(Collectors.toList());
        question.setOptions(opts);

        return question;
    }

    private static QuestionOptionDTO toOptionDTO(QuestionOption opt) {
        QuestionOptionDTO dto = new QuestionOptionDTO();
        dto.setOptionLabel(opt.getOptionLabel());
        dto.setOptionText(opt.getOptionText());
        dto.setCorrect(opt.isCorrect());
        return dto;
    }

    public static QuizSessionDTO toSessionDTO(QuizSession s) {
        QuizSessionDTO dto = new QuizSessionDTO();
        dto.setSessionId(s.getSessionId());
        dto.setQuestions(
                s.getQuestions().stream()
                        .map(DtoMapper::toQuestionDTO)
                        .collect(Collectors.toList())
        );
        dto.setCurrentIndex(s.getCurrentIndex());
        dto.setAnsweredCount(s.getAnsweredCount());
        dto.setCorrectAnswers(s.getCorrectAnswers());
        return dto;
    }
}