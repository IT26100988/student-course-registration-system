package com.example.studentregistration.accounts.dto;

/** PATCH /students/me and PATCH /admin/students/{id}. At least one field required. */
public record UpdateStudentRequest(
        String fullName,
        String email
) {}