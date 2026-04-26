package com.frandm.healthtracker.backend.nutrition.profile;

import com.frandm.healthtracker.backend.nutrition.profile.NutritionProfileDtos.NutritionProfileRequest;
import com.frandm.healthtracker.backend.nutrition.profile.NutritionProfileDtos.NutritionProfileResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutrition/profile")
public class NutritionProfileController {

    private final NutritionProfileService profileService;

    public NutritionProfileController(NutritionProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public NutritionProfileResponse getProfile(@AuthenticationPrincipal UUID userId) {
        return profileService.getProfile(userId);
    }

    @PutMapping
    public NutritionProfileResponse upsertProfile(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody NutritionProfileRequest request
    ) {
        return profileService.upsertProfile(userId, request);
    }
}
