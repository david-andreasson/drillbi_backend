package com.davanddev.quizapp_api.controller;

import com.davanddev.quizapp_api.dto.CourseDTO;
import com.davanddev.quizapp_api.models.Course;
import com.davanddev.quizapp_api.repository.CourseRepository;
import com.davanddev.quizapp_api.util.DtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/courses")
public class CourseController {

    private final CourseRepository courseRepository;

    public CourseController(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getCourses(@AuthenticationPrincipal Jwt jwt) {
        String role = jwt.getClaimAsString("role");
        String group = jwt.getClaimAsString("userGroup");

        List<Course> courses;
        if ("ADMIN".equalsIgnoreCase(role)) {
            courses = courseRepository.findAll();
        } else {
            courses = courseRepository.findByUserGroup(group);
        }

        List<CourseDTO> dtoList = courses.stream()
                .map(DtoMapper::toCourseDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }
}