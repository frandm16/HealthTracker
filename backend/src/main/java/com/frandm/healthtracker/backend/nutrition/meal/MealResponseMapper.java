package com.frandm.healthtracker.backend.nutrition.meal;

import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealDishResponse;
import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealItemResponse;
import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealSlotResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MealResponseMapper {

    private final MealItemRepository mealItemRepository;
    private final MealDishRepository mealDishRepository;
    private final MealDishItemRepository mealDishItemRepository;

    public MealResponseMapper(
            MealItemRepository mealItemRepository,
            MealDishRepository mealDishRepository,
            MealDishItemRepository mealDishItemRepository
    ) {
        this.mealItemRepository = mealItemRepository;
        this.mealDishRepository = mealDishRepository;
        this.mealDishItemRepository = mealDishItemRepository;
    }

    public MealSlotResponse toSlotResponse(MealSlotEntity slot) {
        List<MealItemResponse> items = mealItemRepository.findByMealSlotOrderByCreatedAtAsc(slot).stream()
                .map(this::toItemResponse)
                .toList();
        List<MealDishResponse> dishes = mealDishRepository.findByMealSlotOrderByNameAsc(slot).stream()
                .map(dish -> toDishResponse(dish, mealDishItemRepository.findByMealDish(dish).stream()
                        .map(link -> link.getMealItem().getId())
                        .toList()))
                .toList();
        return new MealSlotResponse(slot.getId(), slot.getMealType(), items, dishes);
    }

    public MealItemResponse toItemResponse(MealItemEntity item) {
        return new MealItemResponse(
                item.getId(),
                item.getFood() == null ? null : item.getFood().getId(),
                item.getFoodName(),
                item.getBrand(),
                item.getQuantityG(),
                item.getCaloriesKcal(),
                item.getProteinG(),
                item.getCarbsG(),
                item.getFatsG()
        );
    }

    public MealDishResponse toDishResponse(MealDishEntity dish, List<UUID> itemIds) {
        return new MealDishResponse(
                dish.getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getCaloriesKcal(),
                dish.getProteinG(),
                dish.getCarbsG(),
                dish.getFatsG(),
                itemIds
        );
    }
}
