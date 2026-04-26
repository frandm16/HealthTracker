package com.frandm.healthtracker.backend.nutrition.meal;

import com.frandm.healthtracker.backend.nutrition.day.NutritionDayEntity;
import com.frandm.healthtracker.backend.nutrition.meal.MealSlotEntity.MealType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealSlotRepository extends JpaRepository<MealSlotEntity, UUID> {
    Optional<MealSlotEntity> findByNutritionDayAndMealType(NutritionDayEntity nutritionDay, MealType mealType);

    List<MealSlotEntity> findByNutritionDayOrderByMealTypeAsc(NutritionDayEntity nutritionDay);
}
