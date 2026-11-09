package com.example.studentregistration.accounts.exception;

/**
 * Thrown when a student is created with an email or studentNo that already exists.
 * Maps to HTTP 409 DUPLICATE_STUDENT.
 */
public class DuplicateStudentException extends RuntimeException {
    public DuplicateStudentException(String message) {
        super(message);
    }
}
