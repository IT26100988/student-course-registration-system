package com.example.studentregistration.accounts.exception;

/**
 * Thrown when login fails (wrong email/password or inactive account).
 * Maps to HTTP 401 AUTHENTICATION_REQUIRED.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}