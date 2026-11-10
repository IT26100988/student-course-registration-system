package com.example.studentregistration.accounts.dto;

/** POST /auth/login body. */
    public record LoginRequest(
        String email,
        String password
) {}
