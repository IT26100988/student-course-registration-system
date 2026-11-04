package com.example.studentregistration.scheduling.model;

public class Lecturer {
    private String lecturerId;
    private String fullName;
    private String email;
    private String department;
    private RecordStatus status;

    public Lecturer() {
    }
    public Lecturer(String lecturerId, String fullName, String email,
                    String department, RecordStatus status) {
        this.lecturerId = lecturerId;
        this.fullName = fullName;
        this.email = email;
        this.department = department;
        this.status = status;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
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