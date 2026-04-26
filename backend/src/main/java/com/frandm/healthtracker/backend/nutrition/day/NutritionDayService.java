package com.frandm.healthtracker.backend.nutrition.day;

import com.frandm.healthtracker.backend.auth.model.UserEntity;
import com.frandm.healthtracker.backend.nutrition.common.NutritionAccessService;
import com.frandm.healthtracker.backend.nutrition.common.NutritionDateRanges;
import com.frandm.healthtracker.backend.nutrition.common.NutritionDateRanges.DateRange;
import com.frandm.healthtracker.backend.nutrition.common.NutritionMath;
import com.frandm.healthtracker.backend.nutrition.day.NutritionDayDtos.NutritionDayRequest;
import com.frandm.healthtracker.backend.nutrition.day.NutritionDayDtos.NutritionDayResponse;
import com.frandm.healthtracker.backend.nutrition.day.NutritionDayDtos.NutritionSummaryResponse;
import com.frandm.healthtracker.backend.nutrition.meal.MealItemEntity;
import com.frandm.healthtracker.backend.nutrition.meal.MealItemRepository;
import com.frandm.healthtracker.backend.nutrition.meal.MealResponseMapper;
import com.frandm.healthtracker.backend.nutrition.meal.MealSlotRepository;
import com.frandm.healthtracker.backend.nutrition.water.WaterLogEntity;
import com.frandm.healthtracker.backend.nutrition.water.WaterLogRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NutritionDayService {

    private static final int DEFAULT_TARGET_CALORIES_KCAL = 1800;
    private static final BigDecimal DEFAULT_TARGET_PROTEIN_G = BigDecimal.valueOf(140);
    private static final BigDecimal DEFAULT_TARGET_CARBS_G = BigDecimal.valueOf(198);
    private static final BigDecimal DEFAULT_TARGET_FATS_G = BigDecimal.valueOf(50);

    private final NutritionAccessService accessService;
    private final NutritionDayRepository nutritionDayRepository;
    private final MealSlotRepository mealSlotRepository;
    private final MealItemRepository mealItemRepository;
    private final MealResponseMapper mealResponseMapper;
    private final WaterLogRepository waterLogRepository;

    public NutritionDayService(
            NutritionAccessService accessService,
            NutritionDayRepository nutritionDayRepository,
            MealSlotRepository mealSlotRepository,
            MealItemRepository mealItemRepository,
            MealResponseMapper mealResponseMapper,
            WaterLogRepository waterLogRepository
    ) {
        this.accessService = accessService;
        this.nutritionDayRepository = nutritionDayRepository;
        this.mealSlotRepository = mealSlotRepository;
        this.mealItemRepository = mealItemRepository;
        this.mealResponseMapper = mealResponseMapper;
        this.waterLogRepository = waterLogRepository;
    }

    @Transactional
    public NutritionDayResponse getDay(UUID userId, LocalDate day) {
        NutritionDayEntity nutritionDay = getOrCreateNutritionDay(userId, day);
        return toResponse(nutritionDay);
    }

    @Transactional
    public NutritionDayResponse upsertDay(UUID userId, LocalDate day, NutritionDayRequest request) {
        UserEntity user = accessService.getUser(userId);
        NutritionDayEntity nutritionDay = nutritionDayRepository.findByUserIdAndDay(userId, day)
                .orElseGet(NutritionDayEntity::new);
        nutritionDay.setUser(user);
        nutritionDay.setDay(day);
        nutritionDay.setRestingCaloriesKcal(request.restingCaloriesKcal());
        nutritionDay.setActiveCaloriesKcal(request.activeCaloriesKcal());
        nutritionDay.setAdjustmentCaloriesKcal(request.adjustmentCaloriesKcal());
        nutritionDay.setTargetCaloriesKcal(request.targetCaloriesKcal());
        nutritionDay.setTargetProteinG(request.targetProteinG());
        nutritionDay.setTargetCarbsG(request.targetCarbsG());
        nutritionDay.setTargetFatsG(request.targetFatsG());
        return toResponse(nutritionDayRepository.save(nutritionDay));
    }

    @Transactional
    public NutritionSummaryResponse getSummary(UUID userId, LocalDate day) {
        NutritionDayEntity nutritionDay = getOrCreateNutritionDay(userId, day);
        List<MealItemEntity> items = mealSlotRepository.findByNutritionDayOrderByMealTypeAsc(nutritionDay).stream()
                .flatMap(slot -> mealItemRepository.findByMealSlotOrderByCreatedAtAsc(slot).stream())
                .toList();
        DateRange range = NutritionDateRanges.dayRange(day);
        int waterMl = waterLogRepository.findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, range.start(), range.end()).stream()
                .mapToInt(WaterLogEntity::getAmountMl)
                .sum();

        return new NutritionSummaryResponse(
                day,
                NutritionMath.sum(items.stream().map(MealItemEntity::getCaloriesKcal).toList()),
                NutritionMath.sum(items.stream().map(MealItemEntity::getProteinG).toList()),
                NutritionMath.sum(items.stream().map(MealItemEntity::getCarbsG).toList()),
                NutritionMath.sum(items.stream().map(MealItemEntity::getFatsG).toList()),
                waterMl
        );
    }

    private NutritionDayEntity getOrCreateNutritionDay(UUID userId, LocalDate day) {
        return nutritionDayRepository.findByUserIdAndDay(userId, day)
                .orElseGet(() -> createDefaultNutritionDay(userId, day));
    }

    private NutritionDayEntity createDefaultNutritionDay(UUID userId, LocalDate day) {
        UserEntity user = accessService.getUser(userId);
        NutritionDayEntity nutritionDay = new NutritionDayEntity();
        nutritionDay.setUser(user);
        nutritionDay.setDay(day);
        nutritionDay.setRestingCaloriesKcal(0);
        nutritionDay.setActiveCaloriesKcal(0);
        nutritionDay.setAdjustmentCaloriesKcal(0);
        nutritionDay.setTargetCaloriesKcal(DEFAULT_TARGET_CALORIES_KCAL);
        nutritionDay.setTargetProteinG(DEFAULT_TARGET_PROTEIN_G);
        nutritionDay.setTargetCarbsG(DEFAULT_TARGET_CARBS_G);
        nutritionDay.setTargetFatsG(DEFAULT_TARGET_FATS_G);
        return nutritionDayRepository.save(nutritionDay);
    }

    private NutritionDayResponse toResponse(NutritionDayEntity nutritionDay) {
        return new NutritionDayResponse(
                nutritionDay.getId(),
                nutritionDay.getDay(),
                nutritionDay.getRestingCaloriesKcal(),
                nutritionDay.getActiveCaloriesKcal(),
                nutritionDay.getAdjustmentCaloriesKcal(),
                nutritionDay.getTargetCaloriesKcal(),
                nutritionDay.getTargetProteinG(),
                nutritionDay.getTargetCarbsG(),
                nutritionDay.getTargetFatsG(),
                mealSlotRepository.findByNutritionDayOrderByMealTypeAsc(nutritionDay).stream()
                        .map(mealResponseMapper::toSlotResponse)
                        .toList()
        );
    }
}
