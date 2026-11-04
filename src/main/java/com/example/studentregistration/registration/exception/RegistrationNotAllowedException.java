package com.example.studentregistration.registration.exception;

public class RegistrationNotAllowedException
        extends RuntimeException {

    public RegistrationNotAllowedException(String message) {
        super(message);
    }
}
