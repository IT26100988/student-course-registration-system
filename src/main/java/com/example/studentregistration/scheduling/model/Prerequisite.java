package com.example.studentregistration.scheduling.model;

import java.time.Instant;

public class Prerequisite {
    private String prerequisiteId;
    private String courseId;
    private String requiredCourseId;
    private RecordStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public Prerequisite() {
    }

    public Prerequisite(String prerequisiteId, String courseId,
                        String requiredCourseId, RecordStatus status,
                        Instant createdAt, Instant updatedAt) {
        this.prerequisiteId = prerequisiteId;
        this.courseId = courseId;
        this.requiredCourseId = requiredCourseId;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getPrerequisiteId() {
        return prerequisiteId;
    }

    public void setPrerequisiteId(String prerequisiteId) {
        this.prerequisiteId = prerequisiteId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}