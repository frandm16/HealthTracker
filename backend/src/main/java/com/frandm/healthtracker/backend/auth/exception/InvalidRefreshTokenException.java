package com.frandm.healthtracker.backend.auth.exception;

public class InvalidRefreshTokenException extends AuthException {

    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
