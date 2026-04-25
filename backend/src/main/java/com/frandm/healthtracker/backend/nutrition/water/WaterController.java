package com.frandm.healthtracker.backend.nutrition.water;

import com.frandm.healthtracker.backend.nutrition.water.WaterDtos.WaterLogRequest;
import com.frandm.healthtracker.backend.nutrition.water.WaterDtos.WaterLogResponse;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutrition/water")
public class WaterController {

    private final WaterService waterService;

    public WaterController(WaterService waterService) {
        this.waterService = waterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaterLogResponse addWater(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody WaterLogRequest request
    ) {
        return waterService.addWater(userId, request);
    }

    @GetMapping
    public List<WaterLogResponse> getWater(
            @AuthenticationPrincipal UUID userId,
            @RequestParam LocalDate day
    ) {
        return waterService.getWater(userId, day);
    }

    @DeleteMapping("/{logId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWater(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID logId
    ) {
        waterService.deleteWater(userId, logId);
    }
}
