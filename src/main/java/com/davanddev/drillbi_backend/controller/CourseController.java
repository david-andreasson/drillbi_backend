package com.davanddev.drillbi_backend.controller;

import com.davanddev.drillbi_backend.dto.CourseDTO;
import com.davanddev.drillbi_backend.models.Course;
import com.davanddev.drillbi_backend.repository.CourseRepository;
import com.davanddev.drillbi_backend.util.DtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/courses")
public class CourseController {
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CourseController.class);

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

    @PostMapping
    public ResponseEntity<?> createCourse(@RequestBody CourseDTO courseDto, @AuthenticationPrincipal Jwt jwt) {
        logger.info("POST /api/v2/courses - name={}, displayName={}, description={}", courseDto.getName(), courseDto.getDisplayName(), courseDto.getDescription());
        try {
            if (courseDto.getName() == null || courseDto.getName().isBlank()) {
                logger.error("createCourse: Saknar kursnamn");
                return ResponseEntity.badRequest().body("error.courseNameRequired");
            }
            if (courseRepository.findByName(courseDto.getName()).isPresent()) {
                logger.error("createCourse: Kursnamnet upptaget: {}", courseDto.getName());
                return ResponseEntity.badRequest().body("error.courseNameTaken");
            }
            String userGroup = jwt.getClaimAsString("userGroup");
            if (userGroup == null || userGroup.isBlank()) userGroup = "UNKNOWN";
            Course course = new Course(
                courseDto.getName(),
                courseDto.getDisplayName(),
                courseDto.getDescription(),
                userGroup
            );
            Course saved = courseRepository.save(course);
            CourseDTO dto = DtoMapper.toCourseDTO(saved);
            logger.info("createCourse: Kurs skapad OK: {}", courseDto.getName());
            return ResponseEntity.status(201).body(dto);
        } catch (Exception e) {
            logger.error("createCourse: Exception/fel", e);
            logger.error("createCourse: Felmeddelande: {}", "error.courseCreateFailed");
            return ResponseEntity.internalServerError().body("error.courseCreateFailed");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable Long id) {
        return courseRepository.findById(id)
            .map(DtoMapper::toCourseDTO)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @RequestBody CourseDTO updatedCourse) {
        return courseRepository.findById(id)
            .map(course -> {
                course.setName(updatedCourse.getName());
                course.setDisplayName(updatedCourse.getDisplayName());
                course.setDescription(updatedCourse.getDescription());
                Course saved = courseRepository.save(course);
                return ResponseEntity.ok(DtoMapper.toCourseDTO(saved));
            })
            .orElse(ResponseEntity.notFound().build());
    }
}