package com.frandm.healthtracker.backend.nutrition.meal;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealDishRepository extends JpaRepository<MealDishEntity, UUID> {
    List<MealDishEntity> findByMealSlotOrderByNameAsc(MealSlotEntity mealSlot);
}
