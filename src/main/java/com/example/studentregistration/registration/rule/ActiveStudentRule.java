package com.example.studentregistration.registration.rule;

import com.example.studentregistration.registration.model.RegistrationRequest;
import org.springframework.stereotype.Component;

@Component
public class ActiveStudentRule implements RegistrationRule {

    @Override
    public boolean isAllowed(RegistrationRequest request) {
        // TODO: M1 Account service integration. For now assume active.
        return request.getStudentId() != null && !request.getStudentId().isBlank();
    }

    @Override
    public String getFailureMessage() {
        return "Student account is inactive.";
    }
}