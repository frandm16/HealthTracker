package com.frandm.healthtracker.backend.nutrition.meal;

import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealDishRequest;
import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealDishResponse;
import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealItemRequest;
import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealItemResponse;
import com.frandm.healthtracker.backend.nutrition.meal.MealSlotEntity.MealType;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutrition")
public class MealController {

    private final MealService mealService;

    public MealController(MealService mealService) {
        this.mealService = mealService;
    }

    @PostMapping("/days/{day}/meals/{mealType}/items")
    @ResponseStatus(HttpStatus.CREATED)
    public MealItemResponse addMealItem(
            @AuthenticationPrincipal UUID userId,
            @PathVariable LocalDate day,
            @PathVariable MealType mealType,
            @Valid @RequestBody MealItemRequest request
    ) {
        return mealService.addMealItem(userId, day, mealType, request);
    }

    @PutMapping("/meal-items/{itemId}")
    public MealItemResponse updateMealItem(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID itemId,
            @Valid @RequestBody MealItemRequest request
    ) {
        return mealService.updateMealItem(userId, itemId, request);
    }

    @DeleteMapping("/meal-items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMealItem(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID itemId
    ) {
        mealService.deleteMealItem(userId, itemId);
    }

    @PostMapping("/days/{day}/meals/{mealType}/dishes")
    @ResponseStatus(HttpStatus.CREATED)
    public MealDishResponse createDish(
            @AuthenticationPrincipal UUID userId,
            @PathVariable LocalDate day,
            @PathVariable MealType mealType,
            @Valid @RequestBody MealDishRequest request
    ) {
        return mealService.createDish(userId, day, mealType, request);
    }

    @DeleteMapping("/meal-dishes/{dishId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDish(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID dishId
    ) {
        mealService.deleteDish(userId, dishId);
    }
}
