package com.example.studentregistration.scheduling.dto;

import java.util.ArrayList;

public class EligibilityResponse {

    private boolean eligible;
    private ArrayList<String> missingPrerequisiteCourseIds;
    private String message;

    public EligibilityResponse() {
        this.missingPrerequisiteCourseIds = new ArrayList<>();
    }

    public EligibilityResponse(
            boolean eligible,
            ArrayList<String> missingPrerequisiteCourseIds,
            String message
    ) {
        this.eligible = eligible;
        this.missingPrerequisiteCourseIds = missingPrerequisiteCourseIds;
        this.message = message;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }

    public ArrayList<String> getMissingPrerequisiteCourseIds() {
        return missingPrerequisiteCourseIds;
    }

    public void setMissingPrerequisiteCourseIds(
            ArrayList<String> missingPrerequisiteCourseIds
    ) {
        this.missingPrerequisiteCourseIds = missingPrerequisiteCourseIds;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}