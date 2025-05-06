package com.davanddev.quizapp_api.repository;

import com.davanddev.quizapp_api.models.DailyAIUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository interface for DailyAIUsage entities.
 */
@Repository
public interface DailyAIUsageRepository extends JpaRepository<DailyAIUsage, Long> {
    Optional<DailyAIUsage> findByUsernameAndUsageDate(String username, LocalDate usageDate);
}