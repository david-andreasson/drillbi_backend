package com.davanddev.drillbi_backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Entity representing a quiz question.
 */
@Entity
@Table(name = "QUESTION")
@Getter
@Setter
@NoArgsConstructor
public class Question {

    /**
     * Unique identifier for the question.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The course this question belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "course_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_question_course")
    )
    private Course course;

    /**
     * The sequential number of the question within the course.
     */
    @Column(name = "question_number", nullable = false)
    private int questionNumber;

    /**
     * The text content of the question.
     */
    @Column(name = "question_text", columnDefinition = "TEXT", nullable = false)
    private String questionText;

    /**
     * URL or path to the image associated with this question (optional).
     */
    @Column(name = "image_url")
    private String imageUrl;

    /**
     * The list of options for this question.
     */
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options;

    /**
     * Retrieves the label of the correct option, if present.
     *
     * @return the label of the correct option, or null if none.
     */
    public String getCorrectOptionLabel() {
        if (options == null) {
            return null;
        }
        return options.stream()
                .filter(QuestionOption::isCorrect)
                .map(QuestionOption::getOptionLabel)
                .findFirst()
                .orElse(null);
    }
}