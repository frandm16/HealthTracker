package com.frandm.healthtracker.backend.nutrition.meal;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealDishItemRepository extends JpaRepository<MealDishItemEntity, UUID> {
    List<MealDishItemEntity> findByMealDish(MealDishEntity mealDish);

    void deleteByMealDish(MealDishEntity mealDish);

    void deleteByMealItem(MealItemEntity mealItem);
}
