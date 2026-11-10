package com.example.studentregistration.scheduling.exception;

public class PrerequisiteNotMetException extends RuntimeException {
    public PrerequisiteNotMetException(String message) {
        super(message);
    }
}