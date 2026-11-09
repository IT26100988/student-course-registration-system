package com.example.studentregistration.accounts.exception;

/**
 * Thrown when a student ID / email does not exist.
 * Maps to HTTP 404 STUDENT_NOT_FOUND.
 */
public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(String message) {
        super(message);
    }
}