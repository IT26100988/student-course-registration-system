package com.example.studentregistration.accounts.dto;

public class RegisterStudentResponse {

    private String studentId;
    private String studentNo;
    private String fullName;
    private String email;
    private String status;

    public RegisterStudentResponse() {
    }

    public RegisterStudentResponse(String studentId, String studentNo,
                                   String fullName, String email,
                                   String status) {
        this.studentId = studentId;
        this.studentNo = studentNo;
        this.fullName = fullName;
        this.email = email;
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getStatus() {
        return status;
    }
}