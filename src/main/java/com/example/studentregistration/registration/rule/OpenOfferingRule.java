package com.example.studentregistration.registration.rule;

import com.example.studentregistration.registration.model.RegistrationRequest;
import org.springframework.stereotype.Component;

@Component
public class OpenOfferingRule implements RegistrationRule {

    @Override
    public boolean isAllowed(RegistrationRequest request) {
        // TODO: M2 Offering Service integration.
        // For now assume offering is valid if ID is present.
        return request.getOfferingId() != null && !request.getOfferingId().isBlank();
    }

    @Override
    public String getFailureMessage() {
        return "Course offering is not open for registration.";
    }
}