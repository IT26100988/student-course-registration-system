package com.example.studentregistration.accounts.controller;

import com.example.studentregistration.accounts.dto.StudentProfileResponse;
import com.example.studentregistration.accounts.dto.UpdateStudentRequest;
import com.example.studentregistration.accounts.service.StudentService;
import com.example.studentregistration.shared.exception.FileStorageException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import com.example.studentregistration.accounts.dto.ChangePasswordRequest;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // Get logged-in student's profile
    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(
            HttpServletRequest request)
            throws FileStorageException {

        HttpSession session = request.getSession(false);

        // Check whether user is logged in
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first"));
        }

        // Check student role
        String role = (String) session.getAttribute("role");

        if (!"STUDENT".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied"));
        }

        // Get student ID from session
        String studentId = (String) session.getAttribute("userId");

        Optional<StudentProfileResponse> profile =
                studentService.getStudentProfile(studentId);

        if (profile.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Student not found"));
        }

        return ResponseEntity.ok(profile.get());
    }

    // Update logged-in student's profile
    @PatchMapping("/me")
    public ResponseEntity<?> updateMyProfile(
            @Valid @RequestBody UpdateStudentRequest updateRequest,
            HttpServletRequest request)
            throws FileStorageException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first"));
        }

        String role = (String) session.getAttribute("role");

        if (!"STUDENT".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied"));
        }

        String studentId = (String) session.getAttribute("userId");

        Optional<StudentProfileResponse> updatedProfile =
                studentService.updateStudentProfile(studentId, updateRequest);

        if (updatedProfile.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Student not found"));
        }

        return ResponseEntity.ok(updatedProfile.get());
    }

    // Change logged-in student's password
    @PostMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest passwordRequest,
            HttpServletRequest request)
            throws FileStorageException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Please login first"));
        }

        String role = (String) session.getAttribute("role");

        if (!"STUDENT".equals(role)) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied"));
        }

        String studentId = (String) session.getAttribute("userId");

        boolean changed = studentService.changeStudentPassword(
                studentId,
                passwordRequest.getCurrentPassword(),
                passwordRequest.getNewPassword()
        );

        if (!changed) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Student not found"));
        }

        return ResponseEntity.ok(
                Map.of("message", "Password changed successfully")
        );
    }
}