package com.frandm.healthtracker.backend.nutrition.common;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public final class NutritionDateRanges {

    private NutritionDateRanges() {
    }

    public static DateRange dayRange(LocalDate day) {
        return dateRange(day, day);
    }

    public static DateRange dateRange(LocalDate from, LocalDate to) {
        OffsetDateTime start = from.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        OffsetDateTime end = to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime().minusNanos(1);
        return new DateRange(start, end);
    }

    public record DateRange(OffsetDateTime start, OffsetDateTime end) {
    }
}
