package com.example.studentregistration.accounts.model;

public class Student extends User {

    private String studentNo;

    public Student(String id, String fullName, String email,
                   String passwordHash, AccountStatus status,
                   String createdAt, String updatedAt,
                   String studentNo) {

        super(id, fullName, email, passwordHash, status, createdAt, updatedAt);

        this.studentNo = studentNo;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }
}