package com.davanddev.drillbi_backend.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "COURSE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name; // Technical name/identifier for the course

    @Column(name = "display_name", nullable = false)
    private String displayName; // User-friendly display name

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "user_group", nullable = false)
    private String userGroup;

    public Course(String name, String displayName, String description, String userGroup) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.userGroup = userGroup;
    }
}