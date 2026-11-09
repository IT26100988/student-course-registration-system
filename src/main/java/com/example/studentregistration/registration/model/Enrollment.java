
package com.example.studentregistration.registration.model;

import java.time.Instant;
import java.util.Objects;

public class Enrollment {

    private final String enrollmentId;
    private final String requestId;
    private final String studentId;
    private final String offeringId;

    private EnrollmentStatus status;

    private final Instant createdAt;
    private Instant holdExpiresAt;
    private Instant activatedAt;
    private Instant closedAt;
    private Instant updatedAt;

    public Enrollment(
            String enrollmentId,
            String requestId,
            String studentId,
            String offeringId,
            EnrollmentStatus status,
            Instant createdAt,
            Instant holdExpiresAt) {

        this(
                enrollmentId,
                requestId,
                studentId,
                offeringId,
                status,
                createdAt,
                holdExpiresAt,
                status == EnrollmentStatus.ACTIVE
                        ? createdAt : null,
                null,
                createdAt
        );
    }

    public Enrollment(
            String enrollmentId,
            String requestId,
            String studentId,
            String offeringId,
            EnrollmentStatus status,
            Instant createdAt,
            Instant holdExpiresAt,
            Instant activatedAt,
            Instant closedAt,
            Instant updatedAt) {

        this.enrollmentId =
                Objects.requireNonNull(enrollmentId);
        this.requestId =
                Objects.requireNonNull(requestId);
        this.studentId =
                Objects.requireNonNull(studentId);
        this.offeringId =
                Objects.requireNonNull(offeringId);

        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);

        if (status == EnrollmentStatus.PENDING_PAYMENT
                && (holdExpiresAt == null
                || !holdExpiresAt.isAfter(createdAt))) {
            throw new IllegalArgumentException(
                    "Valid payment hold expiry required");
        }

        this.holdExpiresAt = holdExpiresAt;
        this.activatedAt = activatedAt;
        this.closedAt = closedAt;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getOfferingId() {
        return offeringId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getHoldExpiresAt() {
        return holdExpiresAt;
    }

    public Instant getActivatedAt() {
        return activatedAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    public void setHoldExpiresAt(Instant holdExpiresAt) {
        this.holdExpiresAt = holdExpiresAt;
    }

    public void setActivatedAt(Instant activatedAt) {
        this.activatedAt = activatedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }
}
