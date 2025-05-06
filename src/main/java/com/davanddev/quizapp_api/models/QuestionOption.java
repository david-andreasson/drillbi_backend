package com.davanddev.quizapp_api.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing an option for a quiz question.
 */
@Entity
@Table(name = "QUESTION_OPTION")
@Getter
@Setter
@NoArgsConstructor
public class QuestionOption {

    /**
     * Unique identifier for the question option.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The question this option belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_option_question"))
    private Question question;

    /**
     * The label for this option (e.g., 'A', 'B', 'C').
     */
    @Column(name = "option_label", nullable = false, length = 1)
    private String optionLabel;

    /**
     * The text content of this option.
     */
    @Column(name = "option_text", columnDefinition = "TEXT", nullable = false)
    private String optionText;

    /**
     * Flag indicating whether this option is the correct answer.
     */
    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;
}
