package com.frandm.healthtracker.backend.nutrition.water;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WaterLogRepository extends JpaRepository<WaterLogEntity, UUID> {
    List<WaterLogEntity> findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(UUID userId, OffsetDateTime start, OffsetDateTime end);
}
