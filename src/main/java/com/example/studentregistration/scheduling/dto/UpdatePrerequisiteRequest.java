package com.example.studentregistration.scheduling.dto;

import com.example.studentregistration.scheduling.model.RecordStatus;

public class UpdatePrerequisiteRequest {

    private String courseId;
    private String requiredCourseId;
    private RecordStatus status;

    public UpdatePrerequisiteRequest() {
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getRequiredCourseId() {
        return requiredCourseId;
    }

    public void setRequiredCourseId(String requiredCourseId) {
        this.requiredCourseId = requiredCourseId;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }
}