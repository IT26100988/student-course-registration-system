package com.example.studentregistration.accounts.dto;

/** Response for student profile endpoints. NEVER include passwordHash. */
public record StudentProfileResponse(
        String studentId,
        String studentNo,
        String fullName,
        String email,
        String status
) {}
