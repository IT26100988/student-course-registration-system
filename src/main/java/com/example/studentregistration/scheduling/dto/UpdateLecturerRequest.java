package com.example.studentregistration.scheduling.dto;

import com.example.studentregistration.scheduling.model.RecordStatus;

public class UpdateLecturerRequest {

    private String fullName;
    private String email;
    private String department;
    private RecordStatus status;

    public UpdateLecturerRequest() {
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }
}