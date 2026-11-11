package com.example.studentregistration.registration.rule;

import com.example.studentregistration.registration.model.RegistrationRequest;
import org.springframework.stereotype.Component;

@Component
public class SchedulePrerequisiteRule implements RegistrationRule {

    @Override
    public boolean isAllowed(RegistrationRequest request) {
        // TODO: M3/M5 integration for prerequisites and timetable conflicts.
        // For now assume true.
        return true;
    }

    @Override
    public String getFailureMessage() {
        return "Prerequisites not met or timetable conflict.";
    }
}