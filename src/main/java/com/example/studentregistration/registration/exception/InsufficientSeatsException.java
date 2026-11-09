package com.example.studentregistration.registration.exception;

public class InsufficientSeatsException extends RuntimeException {

    public InsufficientSeatsException(String message) {
        super(message);
    }
}
