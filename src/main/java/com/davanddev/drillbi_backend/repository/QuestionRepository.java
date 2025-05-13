package com.davanddev.drillbi_backend.repository;

import com.davanddev.drillbi_backend.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    /**
     * Find all questions belonging to a given course name.
     */
    List<Question> findByCourse_Name(String courseName);

    /**
     * Count how many questions exist for a given course name.
     */
    long countByCourse_Name(String courseName);
}