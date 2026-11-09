package com.example.studentregistration.registration.dto;

import java.time.Instant;

public record RegistrationRequestResponse(
        String requestId,
        String studentId,
        String offeringId,
        long sequenceNo,
        String status,
        Instant requestedAt
) {
}