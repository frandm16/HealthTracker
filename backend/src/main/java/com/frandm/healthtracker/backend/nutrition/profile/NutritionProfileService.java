package com.frandm.healthtracker.backend.nutrition.profile;

import com.frandm.healthtracker.backend.auth.model.UserEntity;
import com.frandm.healthtracker.backend.nutrition.common.NutritionAccessService;
import com.frandm.healthtracker.backend.nutrition.profile.NutritionProfileDtos.NutritionProfileRequest;
import com.frandm.healthtracker.backend.nutrition.profile.NutritionProfileDtos.NutritionProfileResponse;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NutritionProfileService {

    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    private final NutritionAccessService accessService;
    private final NutritionProfileRepository profileRepository;

    public NutritionProfileService(NutritionAccessService accessService, NutritionProfileRepository profileRepository) {
        this.accessService = accessService;
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public NutritionProfileResponse getProfile(UUID userId) {
        return profileRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found."));
    }

    @Transactional
    public NutritionProfileResponse upsertProfile(UUID userId, NutritionProfileRequest request) {
        validatePercentages(request.proteinPercentage(), request.carbsPercentage(), request.fatsPercentage());
        UserEntity user = accessService.getUser(userId);
        NutritionProfileEntity profile = profileRepository.findByUserId(userId)
                .orElseGet(NutritionProfileEntity::new);
        profile.setUser(user);
        profile.setProteinPercentage(request.proteinPercentage());
        profile.setCarbsPercentage(request.carbsPercentage());
        profile.setFatsPercentage(request.fatsPercentage());
        return toResponse(profileRepository.save(profile));
    }

    private void validatePercentages(BigDecimal protein, BigDecimal carbs, BigDecimal fats) {
        BigDecimal total = protein.add(carbs).add(fats);
        if (total.compareTo(ONE_HUNDRED) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid request.");
        }
    }

    private NutritionProfileResponse toResponse(NutritionProfileEntity profile) {
        return new NutritionProfileResponse(
                profile.getId(),
                profile.getProteinPercentage(),
                profile.getCarbsPercentage(),
                profile.getFatsPercentage()
        );
    }
}
