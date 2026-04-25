package com.frandm.healthtracker.backend.nutrition.food;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodRepository extends JpaRepository<FoodEntity, UUID> {
    List<FoodEntity> findTop20ByNameContainingIgnoreCaseOrderByNameAsc(String name);

    Optional<FoodEntity> findFirstByBarcode(String barcode);
}
