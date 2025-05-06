package com.davanddev.quizapp_api.repository;

import com.davanddev.quizapp_api.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByName(String name);

    List<Course> findByUserGroup(String userGroup);
}