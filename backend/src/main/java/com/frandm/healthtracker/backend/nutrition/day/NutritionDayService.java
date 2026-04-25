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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NutritionDayService {

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

    @Transactional(readOnly = true)
    public NutritionDayResponse getDay(UUID userId, LocalDate day) {
        NutritionDayEntity nutritionDay = getNutritionDay(userId, day);
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

    @Transactional(readOnly = true)
    public NutritionSummaryResponse getSummary(UUID userId, LocalDate day) {
        NutritionDayEntity nutritionDay = getNutritionDay(userId, day);
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

    private NutritionDayEntity getNutritionDay(UUID userId, LocalDate day) {
        return nutritionDayRepository.findByUserIdAndDay(userId, day)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutrition day was not found."));
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
