package com.davanddev.drillbi_backend.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Entity
@Table(name = "DAILY_AI_USAGE",
        uniqueConstraints = @UniqueConstraint(columnNames = {"username", "usage_date"}))
@Getter
@Setter
@NoArgsConstructor
public class DailyAIUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "request_count", nullable = false)
    private int requestCount;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public DailyAIUsage(String username, LocalDate usageDate, int requestCount) {
        this.username = username;
        this.usageDate = usageDate;
        this.requestCount = requestCount;
    }
}