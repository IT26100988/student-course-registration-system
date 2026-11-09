package com.example.studentregistration.accounts.model;

import java.time.Instant;

/**
 * Student IS-A User.
 * Adds studentNo (unique). Overrides getRole() to return STUDENT.
 */
public class Student extends User {

    private String studentNo; // unique, e.g. "ST2026001"

    public Student(String id,
                   String studentNo,
                   String fullName,
                   String email,
                   String passwordHash,
                   AccountStatus status,
                   Instant createdAt,
                   Instant updatedAt) {
        super(id, fullName, email, passwordHash, status, createdAt, updatedAt);
        this.studentNo = studentNo;
    }

    @Override
    public UserRole getRole() {
        return UserRole.STUDENT;
    }

    public String getStudentNo() { return studentNo; }
    public void setStudentNo(String studentNo) { this.studentNo = studentNo; }
}