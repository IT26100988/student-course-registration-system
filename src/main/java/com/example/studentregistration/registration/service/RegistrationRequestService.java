
package com.example.studentregistration.registration.service;

import com.example.studentregistration.registration.datastructure.RegistrationQueue;
import com.example.studentregistration.registration.exception.RegistrationNotAllowedException;
import com.example.studentregistration.registration.model.Enrollment;
import com.example.studentregistration.registration.model.EnrollmentStatus;
import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.model.RequestStatus;
import com.example.studentregistration.registration.repository.EnrollmentRepository;
import com.example.studentregistration.registration.repository.RegistrationRequestRepository;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class RegistrationRequestService {

    private final RegistrationRequestRepository requestRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RegistrationQueue registrationQueue;

    public RegistrationRequestService(
            RegistrationRequestRepository requestRepository,
            EnrollmentRepository enrollmentRepository,
            RegistrationQueue registrationQueue) {

        this.requestRepository = requestRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.registrationQueue = registrationQueue;
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

        ArrayList<RegistrationRequest> requests =
                requestRepository.findAll();

        for (RegistrationRequest request : requests) {

            boolean sameStudent =
                    studentId.equals(request.getStudentId());

            boolean sameOffering =
                    offeringId.equals(request.getOfferingId());

            if (sameStudent && sameOffering
                    && request.getStatus() == RequestStatus.PENDING) {

                throw new RegistrationNotAllowedException(
                        "You already have a pending request for this offering.");
            }
        }

        ArrayList<Enrollment> enrollments =
                enrollmentRepository.findAll();

        for (Enrollment enrollment : enrollments) {

            boolean sameStudent =
                    studentId.equals(enrollment.getStudentId());

            boolean sameOffering =
                    offeringId.equals(enrollment.getOfferingId());

            boolean liveEnrollment =
                    enrollment.getStatus() == EnrollmentStatus.PENDING_PAYMENT
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

        requestRepository.save(newRequest);

        registrationQueue.enqueue(newRequest);

        return newRequest;
    }
}
