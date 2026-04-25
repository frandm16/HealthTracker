package com.frandm.healthtracker.backend.nutrition.food;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodSearchHistoryRepository extends JpaRepository<FoodSearchHistoryEntity, UUID> {
}
