package com.frandm.healthtracker.backend.nutrition.weight;

import com.frandm.healthtracker.backend.nutrition.common.NutritionAccessService;
import com.frandm.healthtracker.backend.nutrition.common.NutritionDateRanges;
import com.frandm.healthtracker.backend.nutrition.common.NutritionDateRanges.DateRange;
import com.frandm.healthtracker.backend.nutrition.weight.WeightDtos.WeightLogRequest;
import com.frandm.healthtracker.backend.nutrition.weight.WeightDtos.WeightLogResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WeightService {

    private final NutritionAccessService accessService;
    private final WeightLogRepository weightLogRepository;

    public WeightService(NutritionAccessService accessService, WeightLogRepository weightLogRepository) {
        this.accessService = accessService;
        this.weightLogRepository = weightLogRepository;
    }

    @Transactional
    public WeightLogResponse addWeight(UUID userId, WeightLogRequest request) {
        WeightLogEntity log = new WeightLogEntity();
        log.setUser(accessService.getUser(userId));
        log.setWeightKg(request.weightKg());
        log.setRecordedAt(request.recordedAt());
        return toResponse(weightLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<WeightLogResponse> getWeight(UUID userId, LocalDate from, LocalDate to) {
        DateRange range = NutritionDateRanges.dateRange(from, to);
        return weightLogRepository.findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, range.start(), range.end()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteWeight(UUID userId, UUID logId) {
        WeightLogEntity log = weightLogRepository.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weight log was not found."));
        accessService.assertOwned(userId, log.getUser().getId());
        weightLogRepository.delete(log);
    }

    private WeightLogResponse toResponse(WeightLogEntity log) {
        return new WeightLogResponse(log.getId(), log.getWeightKg(), log.getRecordedAt());
    }
}
