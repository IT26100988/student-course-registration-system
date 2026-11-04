package com.example.studentregistration.model;


public class Student extends User {

    private String studentId;

    public Student(String id, String username, String password,
                   String fullName, String email, String studentId) {
        super(id, username, password, fullName, email);
        this.studentId = studentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }
}
