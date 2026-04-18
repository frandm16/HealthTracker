package com.frandm.healthtracker.backend.common;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
        String message,
        List<String> errors, // optional
        OffsetDateTime timestamp
) {
}
