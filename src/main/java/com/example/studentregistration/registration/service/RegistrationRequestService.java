package com.example.studentregistration.registration.service;

import com.example.studentregistration.registration.datastructure.RegistrationQueue;
import com.example.studentregistration.registration.exception.RegistrationNotAllowedException;
import com.example.studentregistration.registration.model.Enrollment;
import com.example.studentregistration.registration.model.EnrollmentStatus;
import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.model.RequestStatus;
import com.example.studentregistration.registration.repository.EnrollmentRepository;
import com.example.studentregistration.registration.repository.RegistrationRequestRepository;
import com.example.studentregistration.registration.rule.RegistrationRule;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RegistrationRequestService {

    private final RegistrationRequestRepository requestRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RegistrationQueue registrationQueue;
    private final List<RegistrationRule> rules;

    public RegistrationRequestService(
            RegistrationRequestRepository requestRepository,
            EnrollmentRepository enrollmentRepository,
            RegistrationQueue registrationQueue,
            List<RegistrationRule> rules) {
        this.requestRepository = requestRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.registrationQueue = registrationQueue;
        this.rules = rules;
    }

    public synchronized ArrayList<RegistrationRequest> findAllRequests() {
        return requestRepository.findAll();
    }

    /**
     * Admin list: return all requests (optionally filtered by status).
     * Sorted by sequenceNo ASC (oldest first) — FIFO display order.
     */
    public synchronized ArrayList<RegistrationRequest> listForAdmin(String status) {
        ArrayList<RegistrationRequest> all = requestRepository.findAll();
        ArrayList<RegistrationRequest> filtered = new ArrayList<>();

        for (RegistrationRequest request : all) {
            if (status == null || status.isBlank()
                    || status.equalsIgnoreCase(request.getStatus().name())) {
                filtered.add(request);
            }
        }

        filtered.sort((a, b) -> Long.compare(a.getSequenceNo(), b.getSequenceNo()));
        return filtered;
    }

    /**
     * FIFO processing: peek at the head of the queue.
     * Returns the OLDEST PENDING request or null if the queue is empty.
     */
    public synchronized RegistrationRequest peekNextPending() {
        return registrationQueue.peek();
    }

    /**
     * After a decision, remove the request from the queue.
     */
    public synchronized RegistrationRequest pollNextPending() {
        return registrationQueue.poll();
    }

    /**
     * Find the enrollment ID (if any) created for a request.
     * Used by the admin decision response.
     */
    public synchronized String findEnrollmentIdForRequest(String requestId) {
        for (Enrollment enrollment : enrollmentRepository.findAll()) {
            if (requestId.equals(enrollment.getRequestId())) {
                return enrollment.getEnrollmentId();
            }
        }
        return null;
    }

    public synchronized RegistrationRequest submitRequest(
            String studentId, String offeringId) {

        if (studentId == null || studentId.isBlank()) {
            throw new RegistrationNotAllowedException(
                    "Student ID is required.");
        }

        if (offeringId == null || offeringId.isBlank()) {
            throw new RegistrationNotAllowedException(
                    "Offering ID is required.");
        }

        ArrayList<RegistrationRequest> requests = requestRepository.findAll();

        for (RegistrationRequest request : requests) {
            boolean sameStudent = studentId.equals(request.getStudentId());
            boolean sameOffering = offeringId.equals(request.getOfferingId());

            if (sameStudent && sameOffering
                    && request.getStatus() == RequestStatus.PENDING) {
                throw new RegistrationNotAllowedException(
                        "You already have a pending request for this offering.");
            }
        }

        ArrayList<Enrollment> enrollments = enrollmentRepository.findAll();

        for (Enrollment enrollment : enrollments) {
            boolean sameStudent = studentId.equals(enrollment.getStudentId());
            boolean sameOffering = offeringId.equals(enrollment.getOfferingId());
            boolean liveEnrollment = enrollment.getStatus() == EnrollmentStatus.PENDING_PAYMENT
                    || enrollment.getStatus() == EnrollmentStatus.ACTIVE;

            if (sameStudent && sameOffering && liveEnrollment) {
                throw new RegistrationNotAllowedException(
                        "You are already enrolled in this offering.");
            }
        }

        long nextSequenceNo = 1;
        for (RegistrationRequest request : requests) {
            if (request.getSequenceNo() >= nextSequenceNo) {
                nextSequenceNo = request.getSequenceNo() + 1;
            }
        }

        String requestId = "REQ_" + UUID.randomUUID();

        RegistrationRequest newRequest = new RegistrationRequest(
                requestId,
                studentId,
                offeringId,
                nextSequenceNo,
                RequestStatus.PENDING,
                Instant.now()
        );

        // 1. Validate against business rules
        for (RegistrationRule rule : rules) {
            if (!rule.isAllowed(newRequest)) {
                throw new RegistrationNotAllowedException(rule.getFailureMessage());
            }
        }

        // 2. Save and Enqueue
        requestRepository.save(newRequest);
        registrationQueue.enqueue(newRequest);

        // 3. Return at the very end
        return newRequest;
    }
}