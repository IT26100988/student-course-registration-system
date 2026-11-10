package com.example.studentregistration.accounts.controller;

import com.example.studentregistration.accounts.dto.RegisterStudentRequest;
import com.example.studentregistration.accounts.dto.StudentProfileResponse;
import com.example.studentregistration.accounts.dto.UpdateStudentRequest;
import com.example.studentregistration.accounts.dto.UpdateStudentStatusRequest;
import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.accounts.service.AuthenticationService;
import com.example.studentregistration.accounts.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin endpoints for student management. Base: /api/v1/admin/students
 * All routes require ADMIN (enforced in Step 13 SecurityConfig).
 */
@RestController
@RequestMapping("/api/v1/admin/students")
public class AdminStudentController {

    private final StudentService studentService;
    private final AuthenticationService authenticationService;

    public AdminStudentController(StudentService studentService,
                                  AuthenticationService authenticationService) {
        this.studentService = studentService;
        this.authenticationService = authenticationService;
    }

    /** POST /api/v1/admin/students — create student (admin-initiated). */
    @PostMapping
    public ResponseEntity<StudentProfileResponse> create(@RequestBody RegisterStudentRequest req) {
        String hashed = authenticationService.hashPassword(req.password());
        Student created = studentService.register(
                req.studentNo(), req.fullName(), req.email(), hashed
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toProfile(created));
    }

    /** GET /api/v1/admin/students?query= */
    @GetMapping
    public ResponseEntity<Map<String, Object>> list(@RequestParam(required = false) String query) {
        List<StudentProfileResponse> items = studentService.search(query).stream()
                .map(this::toProfile)
                .toList();
        return ResponseEntity.ok(Map.of("items", items, "total", items.size()));
    }

    /** GET /api/v1/admin/students/{studentId} */
    @GetMapping("/{studentId}")
    public ResponseEntity<StudentProfileResponse> getOne(@PathVariable String studentId) {
        return ResponseEntity.ok(toProfile(studentService.findById(studentId)));
    }

    /** PATCH /api/v1/admin/students/{studentId} */
    @PatchMapping("/{studentId}")
    public ResponseEntity<StudentProfileResponse> update(@PathVariable String studentId,
                                                         @RequestBody UpdateStudentRequest req) {
        if ((req.fullName() == null || req.fullName().isBlank())
                && (req.email() == null || req.email().isBlank())) {
            throw new IllegalArgumentException("At least one of fullName or email is required");
        }
        Student updated = studentService.updateProfile(studentId, req.fullName(), req.email());
        return ResponseEntity.ok(toProfile(updated));
    }

    /** PATCH /api/v1/admin/students/{studentId}/status */
    @PatchMapping("/{studentId}/status")
    public ResponseEntity<StudentProfileResponse> updateStatus(
            @PathVariable String studentId,
            @RequestBody UpdateStudentStatusRequest req) {
        AccountStatus status = AccountStatus.valueOf(req.status());
        Student updated = studentService.updateStatus(studentId, status);
        return ResponseEntity.ok(toProfile(updated));
    }

    private StudentProfileResponse toProfile(Student s) {
        return new StudentProfileResponse(
                s.getId(), s.getStudentNo(), s.getFullName(), s.getEmail(), s.getStatus().name()
        );
    }
}
