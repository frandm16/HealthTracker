package com.frandm.healthtracker.backend.nutrition.food;

import com.frandm.healthtracker.backend.nutrition.food.FoodDtos.FoodRequest;
import com.frandm.healthtracker.backend.nutrition.food.FoodDtos.FoodResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/nutrition/foods")
public class FoodController {

    private final FoodService foodService;

    public FoodController(FoodService foodService) {
        this.foodService = foodService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FoodResponse createFood(@Valid @RequestBody FoodRequest request) {
        return foodService.createFood(request);
    }

    @GetMapping("/search")
    public List<FoodResponse> searchFoods(
            @AuthenticationPrincipal UUID userId,
            @RequestParam String query
    ) {
        return foodService.searchFoods(userId, query);
    }

    @GetMapping("/barcode/{barcode}")
    public FoodResponse findFoodByBarcode(@PathVariable String barcode) {
        return foodService.findFoodByBarcode(barcode);
    }
}
