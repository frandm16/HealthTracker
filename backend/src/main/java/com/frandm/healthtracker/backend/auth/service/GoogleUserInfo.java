package com.frandm.healthtracker.backend.auth.service;

public record GoogleUserInfo(
        String subject,
        String email,
        String name
) {
}
