package com.frandm.healthtracker.backend.nutrition.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class NutritionMath {

    private NutritionMath() {
    }

    public static BigDecimal sum(List<BigDecimal> values) {
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
}
