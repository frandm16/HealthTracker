package com.frandm.healthtracker.backend.nutrition.day;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionDayRepository extends JpaRepository<NutritionDayEntity, UUID> {
    Optional<NutritionDayEntity> findByUserIdAndDay(UUID userId, LocalDate day);
}
