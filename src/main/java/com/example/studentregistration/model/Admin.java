package com.example.studentregistration.model;


public class Admin extends User {

    private String adminId;

    public Admin(String id, String username, String password,
                 String fullName, String email, String adminId) {
        super(id, username, password, fullName, email);
        this.adminId = adminId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }
}