package com.example.studentregistration.accounts.controller;

import com.example.studentregistration.accounts.dto.ChangePasswordRequest;
import com.example.studentregistration.accounts.dto.StudentProfileResponse;
import com.example.studentregistration.accounts.dto.UpdateStudentRequest;
import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.accounts.service.AuthenticationService;
import com.example.studentregistration.accounts.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Student's own-data endpoints. Base: /api/v1/students/me
 * studentId comes from session (Step 13 finalizes security), NOT from body.
 */
@RestController
@RequestMapping("/api/v1/students/me")
public class StudentController {

    private final StudentService studentService;
    private final AuthenticationService authenticationService;

    public StudentController(StudentService studentService,
                             AuthenticationService authenticationService) {
        this.studentService = studentService;
        this.authenticationService = authenticationService;
    }

    /** GET /api/v1/students/me */
    @GetMapping
    public ResponseEntity<StudentProfileResponse> me() {
        String studentId = currentStudentId();
        Student s = studentService.findById(studentId);
        return ResponseEntity.ok(toProfile(s));
    }

    /** PATCH /api/v1/students/me */
    @PatchMapping
    public ResponseEntity<StudentProfileResponse> update(@RequestBody UpdateStudentRequest req) {
        String studentId = currentStudentId();
        if ((req.fullName() == null || req.fullName().isBlank())
                && (req.email() == null || req.email().isBlank())) {
            throw new IllegalArgumentException("At least one of fullName or email is required");
        }
        Student updated = studentService.updateProfile(studentId, req.fullName(), req.email());
        return ResponseEntity.ok(toProfile(updated));
    }

    /** POST /api/v1/students/me/password */
    @PostMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req) {
        String studentId = currentStudentId();
        Student existing = studentService.findById(studentId);

        // Verify current password
        var user = authenticationService.login(existing.getEmail(), req.currentPassword());
        // (login above also validates status + password)

        String newHash = authenticationService.hashPassword(req.newPassword());
        studentService.updatePasswordHash(studentId, newHash);

        return ResponseEntity.ok(java.util.Map.of("changed", true));
    }

    // -------- session helper (placeholder until Step 13) --------

    private String currentStudentId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null
                || "anonymousUser".equals(auth.getName())) {
            throw new IllegalStateException("Not authenticated — SecurityConfig coming in Step 13");
        }
        // In Step 13, auth.getName() will be set to the student's email/id by the login filter.
        // For now, look up by email:
        return studentService.findByEmail(auth.getName())
                .orElseThrow(() -> new IllegalStateException("No student for session: " + auth.getName()))
                .getId();
    }

    private StudentProfileResponse toProfile(Student s) {
        return new StudentProfileResponse(
                s.getId(), s.getStudentNo(), s.getFullName(), s.getEmail(), s.getStatus().name()
        );
    }
}
