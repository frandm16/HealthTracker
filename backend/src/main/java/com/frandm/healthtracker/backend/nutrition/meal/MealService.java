package com.frandm.healthtracker.backend.nutrition.meal;

import com.frandm.healthtracker.backend.nutrition.common.NutritionAccessService;
import com.frandm.healthtracker.backend.nutrition.common.NutritionMath;
import com.frandm.healthtracker.backend.nutrition.day.NutritionDayEntity;
import com.frandm.healthtracker.backend.nutrition.day.NutritionDayRepository;
import com.frandm.healthtracker.backend.nutrition.food.FoodEntity;
import com.frandm.healthtracker.backend.nutrition.food.FoodRepository;
import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealDishRequest;
import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealDishResponse;
import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealItemRequest;
import com.frandm.healthtracker.backend.nutrition.meal.MealDtos.MealItemResponse;
import com.frandm.healthtracker.backend.nutrition.meal.MealSlotEntity.MealType;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MealService {

    private final NutritionAccessService accessService;
    private final NutritionDayRepository nutritionDayRepository;
    private final MealSlotRepository mealSlotRepository;
    private final FoodRepository foodRepository;
    private final MealItemRepository mealItemRepository;
    private final MealDishRepository mealDishRepository;
    private final MealDishItemRepository mealDishItemRepository;
    private final MealResponseMapper responseMapper;

    public MealService(
            NutritionAccessService accessService,
            NutritionDayRepository nutritionDayRepository,
            MealSlotRepository mealSlotRepository,
            FoodRepository foodRepository,
            MealItemRepository mealItemRepository,
            MealDishRepository mealDishRepository,
            MealDishItemRepository mealDishItemRepository,
            MealResponseMapper responseMapper
    ) {
        this.accessService = accessService;
        this.nutritionDayRepository = nutritionDayRepository;
        this.mealSlotRepository = mealSlotRepository;
        this.foodRepository = foodRepository;
        this.mealItemRepository = mealItemRepository;
        this.mealDishRepository = mealDishRepository;
        this.mealDishItemRepository = mealDishItemRepository;
        this.responseMapper = responseMapper;
    }

    @Transactional
    public MealItemResponse addMealItem(UUID userId, LocalDate day, MealType mealType, MealItemRequest request) {
        MealSlotEntity slot = getOrCreateSlot(userId, day, mealType);
        MealItemEntity item = new MealItemEntity();
        item.setMealSlot(slot);
        applyMealItemRequest(item, request);
        return responseMapper.toItemResponse(mealItemRepository.save(item));
    }

    @Transactional
    public MealItemResponse updateMealItem(UUID userId, UUID itemId, MealItemRequest request) {
        MealItemEntity item = getOwnedItem(userId, itemId);
        applyMealItemRequest(item, request);
        return responseMapper.toItemResponse(mealItemRepository.save(item));
    }

    @Transactional
    public void deleteMealItem(UUID userId, UUID itemId) {
        MealItemEntity item = getOwnedItem(userId, itemId);
        mealDishItemRepository.deleteByMealItem(item);
        mealItemRepository.delete(item);
    }

    @Transactional
    public MealDishResponse createDish(UUID userId, LocalDate day, MealType mealType, MealDishRequest request) {
        if (request.mealItemIds().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dish requires at least one meal item.");
        }
        MealSlotEntity slot = getOrCreateSlot(userId, day, mealType);
        List<MealItemEntity> items = request.mealItemIds().stream()
                .map(itemId -> getOwnedItem(userId, itemId))
                .peek(item -> assertSameSlot(item, slot))
                .toList();

        MealDishEntity dish = new MealDishEntity();
        dish.setMealSlot(slot);
        dish.setName(request.name());
        dish.setDescription(request.description());
        updateDishTotals(dish, items);
        MealDishEntity savedDish = mealDishRepository.save(dish);

        items.forEach(item -> {
            MealDishItemEntity link = new MealDishItemEntity();
            link.setMealDish(savedDish);
            link.setMealItem(item);
            mealDishItemRepository.save(link);
        });

        return responseMapper.toDishResponse(savedDish, items.stream().map(MealItemEntity::getId).toList());
    }

    @Transactional
    public void deleteDish(UUID userId, UUID dishId) {
        MealDishEntity dish = getOwnedDish(userId, dishId);
        mealDishItemRepository.deleteByMealDish(dish);
        mealDishRepository.delete(dish);
    }

    public MealItemEntity getOwnedItem(UUID userId, UUID itemId) {
        MealItemEntity item = mealItemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal item was not found."));
        accessService.assertOwned(userId, item.getMealSlot().getNutritionDay().getUser().getId());
        return item;
    }

    private MealSlotEntity getOrCreateSlot(UUID userId, LocalDate day, MealType mealType) {
        NutritionDayEntity nutritionDay = nutritionDayRepository.findByUserIdAndDay(userId, day)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nutrition day was not found."));
        return mealSlotRepository.findByNutritionDayAndMealType(nutritionDay, mealType)
                .orElseGet(() -> {
                    MealSlotEntity slot = new MealSlotEntity();
                    slot.setNutritionDay(nutritionDay);
                    slot.setMealType(mealType);
                    return mealSlotRepository.save(slot);
                });
    }

    private MealDishEntity getOwnedDish(UUID userId, UUID dishId) {
        MealDishEntity dish = mealDishRepository.findById(dishId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal dish was not found."));
        accessService.assertOwned(userId, dish.getMealSlot().getNutritionDay().getUser().getId());
        return dish;
    }

    private void applyMealItemRequest(MealItemEntity item, MealItemRequest request) {
        FoodEntity food = request.foodId() == null ? null : foodRepository.findById(request.foodId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Food was not found."));

        item.setFood(food);
        item.setFoodName(request.foodName());
        item.setBrand(request.brand());
        item.setQuantityG(request.quantityG());
        item.setCaloriesKcal(request.caloriesKcal());
        item.setProteinG(request.proteinG());
        item.setCarbsG(request.carbsG());
        item.setFatsG(request.fatsG());
    }

    private void assertSameSlot(MealItemEntity item, MealSlotEntity slot) {
        if (!item.getMealSlot().getId().equals(slot.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dish items must belong to the requested meal slot.");
        }
    }

    private void updateDishTotals(MealDishEntity dish, List<MealItemEntity> items) {
        dish.setCaloriesKcal(NutritionMath.sum(items.stream().map(MealItemEntity::getCaloriesKcal).toList()));
        dish.setProteinG(NutritionMath.sum(items.stream().map(MealItemEntity::getProteinG).toList()));
        dish.setCarbsG(NutritionMath.sum(items.stream().map(MealItemEntity::getCarbsG).toList()));
        dish.setFatsG(NutritionMath.sum(items.stream().map(MealItemEntity::getFatsG).toList()));
    }
}
