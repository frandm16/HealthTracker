package com.frandm.healthtracker.backend.nutrition.day;

import com.frandm.healthtracker.backend.nutrition.day.NutritionDayDtos.NutritionDayRequest;
import com.frandm.healthtracker.backend.nutrition.day.NutritionDayDtos.NutritionDayResponse;
import com.frandm.healthtracker.backend.nutrition.day.NutritionDayDtos.NutritionSummaryResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutrition/days")
public class NutritionDayController {

    private final NutritionDayService nutritionDayService;

    public NutritionDayController(NutritionDayService nutritionDayService) {
        this.nutritionDayService = nutritionDayService;
    }

    @GetMapping("/{day}")
    public NutritionDayResponse getDay(
            @AuthenticationPrincipal UUID userId,
            @PathVariable LocalDate day
    ) {
        return nutritionDayService.getDay(userId, day);
    }

    @PutMapping("/{day}")
    public NutritionDayResponse upsertDay(
            @AuthenticationPrincipal UUID userId,
            @PathVariable LocalDate day,
            @Valid @RequestBody NutritionDayRequest request
    ) {
        return nutritionDayService.upsertDay(userId, day, request);
    }

    @GetMapping("/{day}/summary")
    public NutritionSummaryResponse getSummary(
            @AuthenticationPrincipal UUID userId,
            @PathVariable LocalDate day
    ) {
        return nutritionDayService.getSummary(userId, day);
    }
}
