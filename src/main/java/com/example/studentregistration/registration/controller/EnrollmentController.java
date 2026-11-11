package com.example.studentregistration.registration.controller;

import com.example.studentregistration.registration.dto.EnrollmentResponse;
import com.example.studentregistration.registration.dto.WithdrawEnrollmentRequest;
import com.example.studentregistration.registration.model.Enrollment;
import com.example.studentregistration.registration.repository.EnrollmentRepository;
import com.example.studentregistration.registration.service.EnrollmentLifecycleService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentLifecycleService lifecycleService;

    public EnrollmentController(EnrollmentRepository enrollmentRepository,
                                EnrollmentLifecycleService lifecycleService) {
        this.enrollmentRepository = enrollmentRepository;
        this.lifecycleService = lifecycleService;
    }

    /** GET /api/v1/students/me/enrollments */
    @GetMapping("/api/v1/students/me/enrollments")
    public ResponseEntity<Map<String, Object>> myEnrollments(HttpSession session) {
        String studentId = requireStudentId(session);

        List<EnrollmentResponse> items = new ArrayList<>();
        for (Enrollment e : enrollmentRepository.findAll()) {
            if (studentId.equals(e.getStudentId())) {
                items.add(toResponse(e));
            }
        }
        return ResponseEntity.ok(Map.of("items", items, "total", items.size()));
    }

    /** PATCH /api/v1/admin/enrollments/{enrollmentId}/withdraw */
    @PatchMapping("/api/v1/admin/enrollments/{enrollmentId}/withdraw")
    public ResponseEntity<EnrollmentResponse> withdraw(
            @PathVariable String enrollmentId,
            @RequestBody WithdrawEnrollmentRequest body,
            HttpSession session) {

        String adminId = requireAdminId(session);

        if (body == null || body.getReason() == null || body.getReason().isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }

        Enrollment withdrawn = lifecycleService.withdrawEnrollment(
                enrollmentId, adminId, body.getReason());

        return ResponseEntity.ok(toResponse(withdrawn));
    }

    private static String requireStudentId(HttpSession session) {
        if (session == null) {
            throw new IllegalStateException("No active session");
        }

        Object value = session.getAttribute("AUTHENTICATED_USER_ID");

        if (value == null || value.toString().isBlank()) {
            throw new IllegalStateException("Not authenticated");
        }

        Object role = session.getAttribute("AUTHENTICATED_USER_ROLE");

        if (role != null && !"STUDENT".equals(role.toString())) {
            throw new IllegalStateException("Only students can access this endpoint");
        }

        return value.toString();
    }

    private static String requireAdminId(HttpSession session) {
        if (session == null) throw new IllegalStateException("No session");
        Object role = session.getAttribute("AUTHENTICATED_USER_ROLE");
        if (role == null || !"ADMIN".equals(role.toString()))
            throw new IllegalStateException("Not an admin");
        Object id = session.getAttribute("AUTHENTICATED_USER_ID");
        if (id == null || id.toString().isBlank())
            throw new IllegalStateException("No admin ID in session");
        return id.toString();
    }

    private static EnrollmentResponse toResponse(Enrollment e) {
        return new EnrollmentResponse(
                e.getEnrollmentId(),
                e.getRequestId(),
                e.getStudentId(),
                e.getOfferingId(),
                e.getStatus().name(),
                e.getCreatedAt(),
                e.getHoldExpiresAt(),
                e.getActivatedAt(),
                e.getClosedAt()
        );
    }
    /** GET /api/v1/admin/enrollments */
    @GetMapping("/api/v1/admin/enrollments")
    public ResponseEntity<Map<String, Object>> allEnrollments(HttpSession session) {
        // Only admin can see all (SecurityConfig handles hasRole("ADMIN"))
        requireAdminId(session);

        List<EnrollmentResponse> items = new ArrayList<>();
        for (Enrollment e : enrollmentRepository.findAll()) {
            items.add(toResponse(e));
        }
        return ResponseEntity.ok(Map.of("items", items, "total", items.size()));
    }
}