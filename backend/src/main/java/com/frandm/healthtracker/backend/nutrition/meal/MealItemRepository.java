package com.frandm.healthtracker.backend.nutrition.meal;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealItemRepository extends JpaRepository<MealItemEntity, UUID> {
    List<MealItemEntity> findByMealSlotOrderByCreatedAtAsc(MealSlotEntity mealSlot);
}
