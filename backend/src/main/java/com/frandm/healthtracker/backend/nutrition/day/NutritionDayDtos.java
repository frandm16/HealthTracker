package com.frandm.healthtracker.backend.nutrition.day;

import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealSlotResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class NutritionDayDtos {

    private NutritionDayDtos() {
    }

    public record NutritionDayRequest(
            @NotNull @PositiveOrZero Integer restingCaloriesKcal,
            @NotNull @PositiveOrZero Integer activeCaloriesKcal,
            @NotNull Integer adjustmentCaloriesKcal,
            @NotNull @PositiveOrZero Integer targetCaloriesKcal,
            @NotNull @DecimalMin("0.0") BigDecimal targetProteinG,
            @NotNull @DecimalMin("0.0") BigDecimal targetCarbsG,
            @NotNull @DecimalMin("0.0") BigDecimal targetFatsG
    ) {
    }

    public record NutritionDayResponse(
            UUID id,
            LocalDate day,
            Integer restingCaloriesKcal,
            Integer activeCaloriesKcal,
            Integer adjustmentCaloriesKcal,
            Integer targetCaloriesKcal,
            BigDecimal targetProteinG,
            BigDecimal targetCarbsG,
            BigDecimal targetFatsG,
            List<MealSlotResponse> mealSlots
    ) {
    }

    public record NutritionSummaryResponse(
            LocalDate day,
            BigDecimal caloriesKcal,
            BigDecimal proteinG,
            BigDecimal carbsG,
            BigDecimal fatsG,
            Integer waterMl
    ) {
    }
}
