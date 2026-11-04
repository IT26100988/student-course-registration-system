package com.example.studentregistration.accounts.dto;

import jakarta.validation.constraints.Email;

public class UpdateStudentRequest {

    private String fullName;

    @Email(message = "Enter a valid email address")
    private String email;

    public UpdateStudentRequest() {
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
}