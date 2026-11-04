package com.example.studentregistration.accounts.model;

public class Admin extends User {

    private String adminId;

    public Admin(String id, String fullName, String email,
                 String passwordHash, AccountStatus status,
                 String createdAt, String updatedAt,
                 String adminId) {

        super(id, fullName, email, passwordHash, status, createdAt, updatedAt);

        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
}