package com.frandm.healthtracker.backend.nutrition.day;

import com.frandm.healthtracker.backend.auth.model.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "nutrition_days")
public class NutritionDayEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(nullable = false)
    private LocalDate day;

    @Column(name = "resting_calories_kcal", nullable = false)
    private Integer restingCaloriesKcal;

    @Column(name = "active_calories_kcal", nullable = false)
    private Integer activeCaloriesKcal;

    @Column(name = "adjustment_calories_kcal", nullable = false)
    private Integer adjustmentCaloriesKcal;

    @Column(name = "target_calories_kcal", nullable = false)
    private Integer targetCaloriesKcal;

    @Column(name = "target_protein_g", nullable = false)
    private BigDecimal targetProteinG;

    @Column(name = "target_carbs_g", nullable = false)
    private BigDecimal targetCarbsG;

    @Column(name = "target_fats_g", nullable = false)
    private BigDecimal targetFatsG;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (id == null) {
            id = UUID.randomUUID();
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        this.day = day;
    }

    public Integer getRestingCaloriesKcal() {
        return restingCaloriesKcal;
    }

    public void setRestingCaloriesKcal(Integer restingCaloriesKcal) {
        this.restingCaloriesKcal = restingCaloriesKcal;
    }

    public Integer getActiveCaloriesKcal() {
        return activeCaloriesKcal;
    }

    public void setActiveCaloriesKcal(Integer activeCaloriesKcal) {
        this.activeCaloriesKcal = activeCaloriesKcal;
    }

    public Integer getAdjustmentCaloriesKcal() {
        return adjustmentCaloriesKcal;
    }

    public void setAdjustmentCaloriesKcal(Integer adjustmentCaloriesKcal) {
        this.adjustmentCaloriesKcal = adjustmentCaloriesKcal;
    }

    public Integer getTargetCaloriesKcal() {
        return targetCaloriesKcal;
    }

    public void setTargetCaloriesKcal(Integer targetCaloriesKcal) {
        this.targetCaloriesKcal = targetCaloriesKcal;
    }

    public BigDecimal getTargetProteinG() {
        return targetProteinG;
    }

    public void setTargetProteinG(BigDecimal targetProteinG) {
        this.targetProteinG = targetProteinG;
    }

    public BigDecimal getTargetCarbsG() {
        return targetCarbsG;
    }

    public void setTargetCarbsG(BigDecimal targetCarbsG) {
        this.targetCarbsG = targetCarbsG;
    }

    public BigDecimal getTargetFatsG() {
        return targetFatsG;
    }

    public void setTargetFatsG(BigDecimal targetFatsG) {
        this.targetFatsG = targetFatsG;
    }
}
