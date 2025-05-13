package com.davanddev.drillbi_backend.repository;

import com.davanddev.drillbi_backend.models.DailyAIUsage;
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