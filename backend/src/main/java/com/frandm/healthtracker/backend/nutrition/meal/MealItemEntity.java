package com.frandm.healthtracker.backend.nutrition.meal;

import com.frandm.healthtracker.backend.nutrition.food.FoodEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "meal_items")
public class MealItemEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_slot_id", nullable = false)
    private MealSlotEntity mealSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id")
    private FoodEntity food;

    @Column(name = "food_name", nullable = false)
    private String foodName;

    private String brand;

    @Column(name = "quantity_g", nullable = false)
    private BigDecimal quantityG;

    @Column(name = "calories_kcal", nullable = false)
    private BigDecimal caloriesKcal;

    @Column(name = "protein_g", nullable = false)
    private BigDecimal proteinG;

    @Column(name = "carbs_g", nullable = false)
    private BigDecimal carbsG;

    @Column(name = "fats_g", nullable = false)
    private BigDecimal fatsG;

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

    public MealSlotEntity getMealSlot() {
        return mealSlot;
    }

    public void setMealSlot(MealSlotEntity mealSlot) {
        this.mealSlot = mealSlot;
    }

    public FoodEntity getFood() {
        return food;
    }

    public void setFood(FoodEntity food) {
        this.food = food;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public BigDecimal getQuantityG() {
        return quantityG;
    }

    public void setQuantityG(BigDecimal quantityG) {
        this.quantityG = quantityG;
    }

    public BigDecimal getCaloriesKcal() {
        return caloriesKcal;
    }

    public void setCaloriesKcal(BigDecimal caloriesKcal) {
        this.caloriesKcal = caloriesKcal;
    }

    public BigDecimal getProteinG() {
        return proteinG;
    }

    public void setProteinG(BigDecimal proteinG) {
        this.proteinG = proteinG;
    }

    public BigDecimal getCarbsG() {
        return carbsG;
    }

    public void setCarbsG(BigDecimal carbsG) {
        this.carbsG = carbsG;
    }

    public BigDecimal getFatsG() {
        return fatsG;
    }

    public void setFatsG(BigDecimal fatsG) {
        this.fatsG = fatsG;
    }
}
