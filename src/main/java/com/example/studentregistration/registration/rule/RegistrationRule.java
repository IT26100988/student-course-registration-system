package com.example.studentregistration.registration.rule;

import com.example.studentregistration.registration.model.RegistrationRequest;

/**
 * Business rule for validating a registration request.
 * Implementations check one specific rule at a time.
 */
public interface RegistrationRule {
    /**
     * @param request the pending request being validated
     * @return true if the request is allowed by this rule, false otherwise
     */
    boolean isAllowed(RegistrationRequest request);

    /** Human-readable reason when this rule fails. */
    String getFailureMessage();
}