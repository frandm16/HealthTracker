package com.frandm.healthtracker.backend.nutrition.water;

import com.frandm.healthtracker.backend.nutrition.common.NutritionAccessService;
import com.frandm.healthtracker.backend.nutrition.common.NutritionDateRanges;
import com.frandm.healthtracker.backend.nutrition.common.NutritionDateRanges.DateRange;
import com.frandm.healthtracker.backend.nutrition.water.WaterDtos.WaterLogRequest;
import com.frandm.healthtracker.backend.nutrition.water.WaterDtos.WaterLogResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WaterService {

    private final NutritionAccessService accessService;
    private final WaterLogRepository waterLogRepository;

    public WaterService(NutritionAccessService accessService, WaterLogRepository waterLogRepository) {
        this.accessService = accessService;
        this.waterLogRepository = waterLogRepository;
    }

    @Transactional
    public WaterLogResponse addWater(UUID userId, WaterLogRequest request) {
        WaterLogEntity log = new WaterLogEntity();
        log.setUser(accessService.getUser(userId));
        log.setAmountMl(request.amountMl());
        log.setRecordedAt(request.recordedAt());
        return toResponse(waterLogRepository.save(log));
    }

    @Transactional(readOnly = true)
    public List<WaterLogResponse> getWater(UUID userId, LocalDate day) {
        DateRange range = NutritionDateRanges.dayRange(day);
        return waterLogRepository.findByUserIdAndRecordedAtBetweenOrderByRecordedAtAsc(userId, range.start(), range.end()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteWater(UUID userId, UUID logId) {
        WaterLogEntity log = waterLogRepository.findById(logId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found."));
        accessService.assertOwned(userId, log.getUser().getId());
        waterLogRepository.delete(log);
    }

    private WaterLogResponse toResponse(WaterLogEntity log) {
        return new WaterLogResponse(log.getId(), log.getAmountMl(), log.getRecordedAt());
    }
}
