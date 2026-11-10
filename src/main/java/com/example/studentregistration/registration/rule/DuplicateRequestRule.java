package com.example.studentregistration.registration.rule;

import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.model.RequestStatus;
import com.example.studentregistration.registration.repository.RegistrationRequestRepository;

import org.springframework.stereotype.Component;

@Component
public class DuplicateRequestRule implements RegistrationRule {

    private final RegistrationRequestRepository requestRepository;

    public DuplicateRequestRule(RegistrationRequestRepository requestRepository) {
        this.requestRepository = requestRepository;
    }

    @Override
    public boolean isAllowed(RegistrationRequest request) {
        for (RegistrationRequest existing : requestRepository.findAll()) {
            if (existing.getStatus() == RequestStatus.PENDING
                    && existing.getStudentId().equals(request.getStudentId())
                    && existing.getOfferingId().equals(request.getOfferingId())
                    && !existing.getRequestId().equals(request.getRequestId())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getFailureMessage() {
        return "You already have a pending request for this offering.";
    }
}