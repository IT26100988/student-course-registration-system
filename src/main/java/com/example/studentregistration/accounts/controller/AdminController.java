package com.example.studentregistration.accounts.controller;

import com.example.studentregistration.accounts.dto.StudentProfileResponse;
import com.example.studentregistration.accounts.dto.UpdateStudentRequest;
import com.example.studentregistration.accounts.dto.UpdateStudentStatusRequest;
import com.example.studentregistration.accounts.dto.RegisterStudentRequest;
import com.example.studentregistration.accounts.dto.RegisterStudentResponse;

import com.example.studentregistration.accounts.service.StudentService;
import com.example.studentregistration.accounts.service.RegistrationService;

import com.example.studentregistration.shared.exception.FileStorageException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/admin/students")
public class AdminController {

    private final StudentService studentService;
    private final RegistrationService registrationService;

    public AdminController(
            StudentService studentService,
            RegistrationService registrationService) {

        this.studentService = studentService;
        this.registrationService = registrationService;
    }

    // Check whether logged-in user is an admin
    private ResponseEntity<?> checkAdmin(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first"));
        }

        String role = (String) session.getAttribute("role");

        if (!"ADMIN".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Admin access only"));
        }

        return null;
    }

    // Create a new student
    @PostMapping
    public ResponseEntity<?> createStudent(
            @Valid @RequestBody RegisterStudentRequest requestBody,
            HttpServletRequest request)
            throws FileStorageException {

        ResponseEntity<?> accessCheck = checkAdmin(request);

        if (accessCheck != null) {
            return accessCheck;
        }

        try {
            RegisterStudentResponse response =
                    registrationService.registerStudent(requestBody);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }

    // Get all students and search students
    @GetMapping
    public ResponseEntity<?> getAllStudents(
            @RequestParam(required = false) String query,
            HttpServletRequest request)
            throws FileStorageException {

        ResponseEntity<?> accessCheck = checkAdmin(request);

        if (accessCheck != null) {
            return accessCheck;
        }

        List<StudentProfileResponse> students =
                studentService.getAllStudentProfiles(query);

        return ResponseEntity.ok(
                Map.of(
                        "items", students,
                        "total", students.size()
                )
        );
    }

    // Get student by ID
    @GetMapping("/{studentId}")
    public ResponseEntity<?> getStudentById(
            @PathVariable String studentId,
            HttpServletRequest request)
            throws FileStorageException {

        ResponseEntity<?> accessCheck = checkAdmin(request);

        if (accessCheck != null) {
            return accessCheck;
        }

        Optional<StudentProfileResponse> student =
                studentService.getStudentProfile(studentId);

        if (student.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Student not found"));
        }

        return ResponseEntity.ok(student.get());
    }

    // Update student details
    @PatchMapping("/{studentId}")
    public ResponseEntity<?> updateStudent(
            @PathVariable String studentId,
            @Valid @RequestBody UpdateStudentRequest updateRequest,
            HttpServletRequest request)
            throws FileStorageException {

        ResponseEntity<?> accessCheck = checkAdmin(request);

        if (accessCheck != null) {
            return accessCheck;
        }

        Optional<StudentProfileResponse> updatedStudent =
                studentService.updateStudentProfile(
                        studentId, updateRequest);

        if (updatedStudent.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Student not found"));
        }

        return ResponseEntity.ok(updatedStudent.get());
    }

    // Update student status
    @PatchMapping("/{studentId}/status")
    public ResponseEntity<?> updateStudentStatus(
            @PathVariable String studentId,
            @RequestBody UpdateStudentStatusRequest updateRequest,
            HttpServletRequest request)
            throws FileStorageException {

        ResponseEntity<?> accessCheck = checkAdmin(request);

        if (accessCheck != null) {
            return accessCheck;
        }

        Optional<StudentProfileResponse> updatedStudent =
                studentService.updateStudentStatus(
                        studentId, updateRequest);

        if (updatedStudent.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Student not found"));
        }

        return ResponseEntity.ok(updatedStudent.get());
    }
}