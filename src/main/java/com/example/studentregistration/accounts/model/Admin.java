package com.example.studentregistration.accounts.model;

import java.time.Instant;

/**
 * Admin IS-A User.
 * No extra fields needed for v1. Overrides getRole() to return ADMIN.
 */
public class Admin extends User {

    public Admin(String id,
                 String fullName,
                 String email,
                 String passwordHash,
                 AccountStatus status,
                 Instant createdAt,
                 Instant updatedAt) {
        super(id, fullName, email, passwordHash, status, createdAt, updatedAt);
    }

    @Override
    public UserRole getRole() {
        return UserRole.ADMIN;
    }
}