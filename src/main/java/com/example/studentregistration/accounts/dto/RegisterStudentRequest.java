package com.example.studentregistration.accounts.dto;

/** POST /auth/register and POST /admin/students body. */
public record RegisterStudentRequest(
        String studentNo,
        String fullName,
        String email,
        String password
) {}