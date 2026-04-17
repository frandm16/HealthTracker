package com.frandm.healthtracker.backend.auth.service;

public interface GoogleTokenValidator {

    GoogleUserInfo validate(String idToken);
}
