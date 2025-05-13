package com.davanddev.drillbi_backend.repository;

import com.davanddev.drillbi_backend.models.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByName(String name);

    List<Course> findByUserGroup(String userGroup);
}