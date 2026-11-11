package com.example.studentregistration.registration.rule;

import com.example.studentregistration.registration.model.Enrollment;
import com.example.studentregistration.registration.model.EnrollmentStatus;
import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.repository.EnrollmentRepository;

import org.springframework.stereotype.Component;

@Component
public class CapacityRule implements RegistrationRule {

    private final EnrollmentRepository enrollmentRepository;

    public CapacityRule(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public boolean isAllowed(RegistrationRequest request) {
        // TODO: M2 offering capacity integration.
        // For now, assume capacity 100. Count active/pending enrollments.
        long currentCount = 0;
        for (Enrollment e : enrollmentRepository.findAll()) {
            if (e.getOfferingId().equals(request.getOfferingId())
                    && (e.getStatus() == EnrollmentStatus.ACTIVE
                    || e.getStatus() == EnrollmentStatus.PENDING_PAYMENT)) {
                currentCount++;
            }
        }
        return currentCount < 100;
    }

    @Override
    public String getFailureMessage() {
        return "No seats remain for this offering.";
    }
}