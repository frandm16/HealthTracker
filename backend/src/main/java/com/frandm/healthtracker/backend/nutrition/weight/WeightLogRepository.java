package com.frandm.healthtracker.backend.nutrition.weight;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeightLogRepository extends JpaRepository<WeightLogEntity, UUID> {
    List<WeightLogEntity> findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(UUID userId, OffsetDateTime start, OffsetDateTime end);
}
