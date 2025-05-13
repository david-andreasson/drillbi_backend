package com.davanddev.drillbi_backend.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegenerateOptionsRequest {
    private String sourceText;
    private String questionText;
    private String language;
    private String courseName;

    @JsonAlias({ "ai", "aiModel" })
    private String aiModel;
}