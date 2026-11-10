package com.example.studentregistration.registration.dto;

import java.time.Instant;

public record EnrollmentResponse(
        String enrollmentId,
        String requestId,
        String studentId,
        String offeringId,
        String status,
        Instant createdAt,
        Instant holdExpiresAt,
        Instant activatedAt,
        Instant closedAt
) {}