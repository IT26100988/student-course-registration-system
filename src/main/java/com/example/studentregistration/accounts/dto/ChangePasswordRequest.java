package com.example.studentregistration.accounts.dto;

/** POST /students/me/password body. */
public record ChangePasswordRequest(
        String currentPassword,
        String newPassword
) {}
