package com.example.studentregistration.accounts.controller;

import com.example.studentregistration.accounts.dto.StudentProfileResponse;
import com.example.studentregistration.accounts.dto.UpdateStudentRequest;
import com.example.studentregistration.accounts.dto.UpdateStudentStatusRequest;
import com.example.studentregistration.accounts.service.StudentService;
import com.example.studentregistration.shared.exception.FileStorageException;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/students")
public class AdminController {

    private final StudentService studentService;

    public AdminController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Get all students and search students
    @GetMapping
    public Map<String, Object> getAllStudents(
            @RequestParam(required = false) String query)
            throws FileStorageException {

        List<StudentProfileResponse> students =
                studentService.getAllStudentProfiles(query);

        return Map.of(
                "items", students,
                "total", students.size()
        );
    }

    // Get student by ID
    @GetMapping("/{studentId}")
    public Optional<StudentProfileResponse> getStudentById(
            @PathVariable String studentId)
            throws FileStorageException {

        return studentService.getStudentProfile(studentId);
    }

    // Update student details
    @PatchMapping("/{studentId}")
    public Optional<StudentProfileResponse> updateStudent(
            @PathVariable String studentId,
            @RequestBody UpdateStudentRequest request)
            throws FileStorageException {

        return studentService.updateStudentProfile(
                studentId, request);
    }

    // Update student status
    @PatchMapping("/{studentId}/status")
    public Optional<StudentProfileResponse> updateStudentStatus(
            @PathVariable String studentId,
            @RequestBody UpdateStudentStatusRequest request)
            throws FileStorageException {

        return studentService.updateStudentStatus(
                studentId, request);
    }
}