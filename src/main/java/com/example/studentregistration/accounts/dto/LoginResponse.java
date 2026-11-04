package com.example.studentregistration.accounts.dto;

public class LoginResponse {

    private String userId;
    private String role;
    private String fullName;

    public LoginResponse() {
    }

    public LoginResponse(String userId, String role, String fullName) {
        this.userId = userId;
        this.role = role;
        this.fullName = fullName;
    }

    public String getUserId() {
        return userId;
    }

    public String getRole() {
        return role;
    }

    public String getFullName() {
        return fullName;
    }
}
