package com.davanddev.drillbi_backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class QuestionOptionDTO {

    @JsonProperty("isCorrect")
    private boolean isCorrect;

    private String optionLabel;
    private String optionText;

    public QuestionOptionDTO() {
    }

    public QuestionOptionDTO(String optionLabel, String optionText, boolean isCorrect) {
        this.optionLabel = optionLabel;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }
}
