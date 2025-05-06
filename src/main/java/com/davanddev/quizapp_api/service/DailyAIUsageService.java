package com.davanddev.quizapp_api.service;

import com.davanddev.quizapp_api.models.DailyAIUsage;
import com.davanddev.quizapp_api.repository.DailyAIUsageRepository;
import com.davanddev.quizapp_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DailyAIUsageService {

    private final DailyAIUsageRepository dailyAIUsageRepository;
    private final UserRepository userRepository;

    private final int DAILY_LIMIT = 50;

    public DailyAIUsageService(DailyAIUsageRepository dailyAIUsageRepository, UserRepository userRepository) {
        this.dailyAIUsageRepository = dailyAIUsageRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public boolean hasExceededDailyLimit(String username) {
        LocalDate today = LocalDate.now();
        return dailyAIUsageRepository.findByUsernameAndUsageDate(username, today)
                .map(usage -> usage.getRequestCount() >= DAILY_LIMIT)
                .orElse(false);
    }

    @Transactional
    public void incrementUsage(String username) {
        LocalDate today = LocalDate.now();

        Long userId = userRepository.findUserIdByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        DailyAIUsage usage = dailyAIUsageRepository.findByUsernameAndUsageDate(username, today)
                .orElse(new DailyAIUsage(username, today, 0));

        usage.setRequestCount(usage.getRequestCount() + 1);
        usage.setUserId(userId);

        dailyAIUsageRepository.save(usage);
    }
}
