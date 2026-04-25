package com.frandm.healthtracker.backend.nutrition.meal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "meal_dishes_items")
public class MealDishItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_dish_id", nullable = false)
    private MealDishEntity mealDish;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_item_id", nullable = false)
    private MealItemEntity mealItem;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public MealDishEntity getMealDish() {
        return mealDish;
    }

    public void setMealDish(MealDishEntity mealDish) {
        this.mealDish = mealDish;
    }

    public MealItemEntity getMealItem() {
        return mealItem;
    }

    public void setMealItem(MealItemEntity mealItem) {
        this.mealItem = mealItem;
    }
}
