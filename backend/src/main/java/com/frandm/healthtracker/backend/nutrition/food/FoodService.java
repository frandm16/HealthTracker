package com.frandm.healthtracker.backend.nutrition.food;

import com.frandm.healthtracker.backend.nutrition.common.NutritionAccessService;
import com.frandm.healthtracker.backend.nutrition.food.FoodDtos.FoodRequest;
import com.frandm.healthtracker.backend.nutrition.food.FoodDtos.FoodResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FoodService {

    private final NutritionAccessService accessService;
    private final FoodRepository foodRepository;
    private final FoodSearchHistoryRepository searchHistoryRepository;

    public FoodService(
            NutritionAccessService accessService,
            FoodRepository foodRepository,
            FoodSearchHistoryRepository searchHistoryRepository
    ) {
        this.accessService = accessService;
        this.foodRepository = foodRepository;
        this.searchHistoryRepository = searchHistoryRepository;
    }

    @Transactional
    public FoodResponse createFood(FoodRequest request) {
        FoodEntity food = new FoodEntity();
        food.setSource(request.source());
        food.setSourceId(request.sourceId());
        food.setBarcode(request.barcode());
        food.setName(request.name());
        food.setBrand(request.brand());
        food.setCaloriesPer100g(request.caloriesPer100g());
        food.setProteinPer100g(request.proteinPer100g());
        food.setCarbsPer100g(request.carbsPer100g());
        food.setFatPer100g(request.fatPer100g());
        return toResponse(foodRepository.save(food));
    }

    @Transactional
    public List<FoodResponse> searchFoods(UUID userId, String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.length() < 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search query must have at least 2 characters.");
        }

        FoodSearchHistoryEntity history = new FoodSearchHistoryEntity();
        history.setUser(accessService.getUser(userId));
        history.setSearchText(normalizedQuery);
        searchHistoryRepository.save(history);

        return foodRepository.findTop20ByNameContainingIgnoreCaseOrderByNameAsc(normalizedQuery).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public FoodResponse findFoodByBarcode(String barcode) {
        return foodRepository.findFirstByBarcode(barcode)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food barcode was not found."));
    }

    public FoodResponse toResponse(FoodEntity food) {
        return new FoodResponse(
                food.getId(),
                food.getSource(),
                food.getSourceId(),
                food.getBarcode(),
                food.getName(),
                food.getBrand(),
                food.getCaloriesPer100g(),
                food.getProteinPer100g(),
                food.getCarbsPer100g(),
                food.getFatPer100g()
        );
    }
}
