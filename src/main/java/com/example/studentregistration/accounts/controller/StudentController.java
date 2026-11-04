package com.example.studentregistration.accounts.controller;

import com.example.studentregistration.accounts.dto.StudentProfileResponse;
import com.example.studentregistration.accounts.dto.UpdateStudentRequest;
import com.example.studentregistration.accounts.service.StudentService;
import com.example.studentregistration.shared.exception.FileStorageException;

import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Get logged-in student's profile
    @GetMapping("/me")
    public Optional<StudentProfileResponse> getMyProfile(
            @RequestParam String studentId)
            throws FileStorageException {

        return studentService.getStudentProfile(studentId);
    }

    // Update student profile
    @PatchMapping("/me")
    public Optional<StudentProfileResponse> updateMyProfile(
            @RequestParam String studentId,
            @RequestBody UpdateStudentRequest request)
            throws FileStorageException {

        return studentService.updateStudentProfile(studentId, request);
    }
}