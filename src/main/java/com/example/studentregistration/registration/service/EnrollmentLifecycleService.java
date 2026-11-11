package com.example.studentregistration.registration.service;

import com.example.studentregistration.registration.exception.QueueEmptyException;
import com.example.studentregistration.registration.exception.RegistrationNotAllowedException;
import com.example.studentregistration.registration.model.Enrollment;
import com.example.studentregistration.registration.model.EnrollmentStatus;
import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.model.RequestStatus;
import com.example.studentregistration.registration.repository.EnrollmentRepository;
import com.example.studentregistration.registration.repository.RegistrationRequestRepository;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
public class EnrollmentLifecycleService {

    /** Payment hold duration for paid offerings. */
    public static final Duration PAYMENT_HOLD =
            Duration.ofHours(24);

    private final RegistrationRequestRepository requestRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentLifecycleService(
            RegistrationRequestRepository requestRepository,
            EnrollmentRepository enrollmentRepository) {

        this.requestRepository = requestRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Approve a single PENDING request and create its Enrollment.
     *
     * @param requestId        request to approve (must be PENDING)
     * @param adminId          authenticated admin performing the action
     * @param reason           optional decision reason
     * @param isPaidOffering   true → PENDING_PAYMENT, false → ACTIVE
     * @return updated request after approval
     */
    public synchronized RegistrationRequest approveRequest(
            String requestId,
            String adminId,
            String reason,
            boolean isPaidOffering) {

        validateAdminId(adminId);

        RegistrationRequest request = loadPendingRequest(requestId);

        Instant now = Instant.now();

        // Mark the request first (source of truth)
        request.setStatus(RequestStatus.APPROVED);
        request.setProcessedAt(now);
        request.setProcessedByAdminId(adminId);
        request.setDecisionReason(reason);

        requestRepository.update(request);

        // Create enrollment (one per request)
        Enrollment enrollment = buildEnrollment(
                request, now, isPaidOffering);

        enrollmentRepository.save(enrollment);

        return request;
    }

    /**
     * Reject a single PENDING request. No enrollment is created.
     */
    public synchronized RegistrationRequest rejectRequest(
            String requestId,
            String adminId,
            String reason) {

        validateAdminId(adminId);

        if (reason == null || reason.isBlank()) {
            throw new RegistrationNotAllowedException(
                    "A rejection reason is required.");
        }

        RegistrationRequest request = loadPendingRequest(requestId);

        request.setStatus(RequestStatus.REJECTED);
        request.setProcessedAt(Instant.now());
        request.setProcessedByAdminId(adminId);
        request.setDecisionReason(reason.trim());

        requestRepository.update(request);

        return request;
    }

    /**
     * Cancel a PENDING request by its owner student.
     * Only the owning student may cancel.
     */
    public synchronized RegistrationRequest cancelByStudent(
            String requestId,
            String studentId) {

        if (studentId == null || studentId.isBlank()) {
            throw new RegistrationNotAllowedException(
                    "Student ID is required.");
        }

        RegistrationRequest request = loadPendingRequest(requestId);

        if (!studentId.equals(request.getStudentId())) {
            throw new RegistrationNotAllowedException(
                    "You can only cancel your own request.");
        }

        request.setStatus(RequestStatus.CANCELLED);
        request.setCancelledAt(Instant.now());

        requestRepository.update(request);

        return request;
    }

    /**
     * Withdraw an ACTIVE enrollment by admin.
     */
    public synchronized Enrollment withdrawEnrollment(
            String enrollmentId,
            String adminId,
            String reason) {

        validateAdminId(adminId);

        if (reason == null || reason.isBlank()) {
            throw new RegistrationNotAllowedException(
                    "A withdrawal reason is required.");
        }

        Enrollment enrollment = enrollmentRepository
                .findById(enrollmentId)
                .orElseThrow(() -> new RegistrationNotAllowedException(
                        "Enrollment not found: " + enrollmentId));

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new RegistrationNotAllowedException(
                    "Only ACTIVE enrollments can be withdrawn.");
        }

        Instant now = Instant.now();

        enrollment.setStatus(EnrollmentStatus.WITHDRAWN);
        enrollment.setClosedAt(now);
        enrollment.setUpdatedAt(now);

        enrollmentRepository.update(enrollment);

        return enrollment;
    }

    /**
     * Activate a PENDING_PAYMENT enrollment after verified payment.
     * Called by M6 through a small service integration method.
     * M6 MUST NOT write enrollments.txt directly.
     */
    public synchronized Enrollment activateAfterPayment(
            String enrollmentId) {

        Enrollment enrollment = enrollmentRepository
                .findById(enrollmentId)
                .orElseThrow(() -> new RegistrationNotAllowedException(
                        "Enrollment not found: " + enrollmentId));

        if (enrollment.getStatus() != EnrollmentStatus.PENDING_PAYMENT) {
            throw new RegistrationNotAllowedException(
                    "Only PENDING_PAYMENT enrollments can be activated.");
        }

        Instant now = Instant.now();

        if (enrollment.getHoldExpiresAt() != null
                && now.isAfter(enrollment.getHoldExpiresAt())) {
            throw new RegistrationNotAllowedException(
                    "Payment hold has already expired.");
        }

        enrollment.setStatus(EnrollmentStatus.ACTIVE);
        enrollment.setActivatedAt(now);
        enrollment.setUpdatedAt(now);

        enrollmentRepository.update(enrollment);

        return enrollment;
    }

    // ---------- helpers ----------

    private RegistrationRequest loadPendingRequest(String requestId) {

        if (requestId == null || requestId.isBlank()) {
            throw new RegistrationNotAllowedException(
                    "Request ID is required.");
        }

        RegistrationRequest request = requestRepository
                .findById(requestId)
                .orElseThrow(() -> new RegistrationNotAllowedException(
                        "Request not found: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RegistrationNotAllowedException(
                    "Only PENDING requests can be processed.");
        }

        return request;
    }

    private Enrollment buildEnrollment(
            RegistrationRequest request,
            Instant now,
            boolean isPaidOffering) {

        String enrollmentId = "ENR_" + UUID.randomUUID();

        if (isPaidOffering) {
            Instant holdExpiresAt = now.plus(PAYMENT_HOLD);

            return new Enrollment(
                    enrollmentId,
                    request.getRequestId(),
                    request.getStudentId(),
                    request.getOfferingId(),
                    EnrollmentStatus.PENDING_PAYMENT,
                    now,
                    holdExpiresAt
            );
        }

        // Free offering → ACTIVE immediately
        return new Enrollment(
                enrollmentId,
                request.getRequestId(),
                request.getStudentId(),
                request.getOfferingId(),
                EnrollmentStatus.ACTIVE,
                now,
                null
        );
    }

    private static void validateAdminId(String adminId) {
        if (adminId == null || adminId.isBlank()) {
            throw new RegistrationNotAllowedException(
                    "Admin ID is required.");
        }
    }
}