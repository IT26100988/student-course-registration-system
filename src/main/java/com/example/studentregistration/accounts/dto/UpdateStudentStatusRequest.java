package com.example.studentregistration.accounts.dto;

/** PATCH /admin/students/{id}/status. status: "ACTIVE" or "INACTIVE". */
public record UpdateStudentStatusRequest(
        String status
) {}