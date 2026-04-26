package com.frandm.healthtracker.backend.nutrition.profile;

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
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "nutrition_profiles")
public class NutritionProfileEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "protein_percentage", nullable = false)
    private BigDecimal proteinPercentage;

    @Column(name = "carbs_percentage", nullable = false)
    private BigDecimal carbsPercentage;

    @Column(name = "fats_percentage", nullable = false)
    private BigDecimal fatsPercentage;

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

    public BigDecimal getProteinPercentage() {
        return proteinPercentage;
    }

    public void setProteinPercentage(BigDecimal proteinPercentage) {
        this.proteinPercentage = proteinPercentage;
    }

    public BigDecimal getCarbsPercentage() {
        return carbsPercentage;
    }

    public void setCarbsPercentage(BigDecimal carbsPercentage) {
        this.carbsPercentage = carbsPercentage;
    }

    public BigDecimal getFatsPercentage() {
        return fatsPercentage;
    }

    public void setFatsPercentage(BigDecimal fatsPercentage) {
        this.fatsPercentage = fatsPercentage;
    }
}
