package com.example.studentregistration.scheduling.dto;

public class CreatePrerequisiteRequest {

    private String courseId;
    private String requiredCourseId;

    public CreatePrerequisiteRequest() {
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
}