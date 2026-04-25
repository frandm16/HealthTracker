package com.frandm.healthtracker.backend.nutrition.profile;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionProfileRepository extends JpaRepository<NutritionProfileEntity, UUID> {
    Optional<NutritionProfileEntity> findByUserId(UUID userId);
}
