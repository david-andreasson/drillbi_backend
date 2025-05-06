package com.davanddev.quizapp_api.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SessionStatsDTO {
    private int score;
    private int total;
    private double errorRate;

    public SessionStatsDTO() {}

    public SessionStatsDTO(int score, int total, double errorRate) {
        this.score = score;
        this.total = total;
        this.errorRate = errorRate;
    }

}