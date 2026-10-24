
package com.example.studentregistration.registration.model;

import java.time.Instant;
import java.util.Objects;

public class RegistrationRequest {

    private final String requestId;
    private final String studentId;
    private final String offeringId;
    private final long sequenceNo;

    private RequestStatus status;
    private final Instant requestedAt;

    private Instant processedAt;
    private String processedByAdminId;
    private String decisionReason;
    private Instant cancelledAt;

    public RegistrationRequest(
            String requestId,
            String studentId,
            String offeringId,
            long sequenceNo,
            RequestStatus status,
            Instant requestedAt) {

        this(
                requestId,
                studentId,
                offeringId,
                sequenceNo,
                status,
                requestedAt,
                null,
                null,
                null,
                null
        );
    }

    public RegistrationRequest(
            String requestId,
            String studentId,
            String offeringId,
            long sequenceNo,
            RequestStatus status,
            Instant requestedAt,
            Instant processedAt,
            String processedByAdminId,
            String decisionReason,
            Instant cancelledAt) {

        this.requestId = Objects.requireNonNull(requestId);
        this.studentId = Objects.requireNonNull(studentId);
        this.offeringId = Objects.requireNonNull(offeringId);

        if (sequenceNo <= 0) {
            throw new IllegalArgumentException(
                    "Sequence number must be positive");
        }

        this.sequenceNo = sequenceNo;
        this.status = Objects.requireNonNull(status);
        this.requestedAt = Objects.requireNonNull(requestedAt);
        this.processedAt = processedAt;
        this.processedByAdminId = processedByAdminId;
        this.decisionReason = decisionReason;
        this.cancelledAt = cancelledAt;
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

    public long getSequenceNo() {
        return sequenceNo;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public String getProcessedByAdminId() {
        return processedByAdminId;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public Instant getCancelledAt() {
        return cancelledAt;
    }

    public void setStatus(RequestStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    public void setProcessedByAdminId(
            String processedByAdminId) {
        this.processedByAdminId = processedByAdminId;
    }

    public void setDecisionReason(String decisionReason) {
        this.decisionReason = decisionReason;
    }

    public void setCancelledAt(Instant cancelledAt) {
        this.cancelledAt = cancelledAt;
    }
}
