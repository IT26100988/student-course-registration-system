package com.example.studentregistration.registration.controller;

import com.example.studentregistration.registration.dto.RegistrationRequestResponse;
import com.example.studentregistration.registration.dto.SubmitRegistrationRequest;
import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.service.RegistrationRequestService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.CrossOrigin;

/**
 * Student-owned registration endpoints.
 * Base: /api/v1/students/me
 *
 * studentId is NEVER accepted from the client.
 * It is always read from the authenticated HTTP session.
 */
@RestController
@RequestMapping("/api/v1/students/me")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class StudentRegistrationController {

    private final RegistrationRequestService requestService;

    public StudentRegistrationController(
            RegistrationRequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * POST /api/v1/students/me/requests
     * Body: { "offeringId": "OFF_..." }
     * 201 Created + RegistrationRequestResponse
     */
    @PostMapping("/requests")
    public ResponseEntity<RegistrationRequestResponse> submit(
            @RequestBody SubmitRegistrationRequest body,
            HttpSession session) {

        String studentId = requireStudentId(session);

        if (body == null || body.getOfferingId() == null
                || body.getOfferingId().isBlank()) {
            throw new IllegalArgumentException(
                    "offeringId is required");
        }

        RegistrationRequest created = requestService.submitRequest(
                studentId,
                body.getOfferingId().trim()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(created));
    }


    @GetMapping("/requests")
    public ResponseEntity<Map<String, Object>> myRequests(
            HttpSession session) {

        String studentId = requireStudentId(session);

        ArrayList<RegistrationRequest> all =
                requestService.findAllRequests();

        List<RegistrationRequestResponse> mine = new ArrayList<>();

        for (RegistrationRequest request : all) {
            if (studentId.equals(request.getStudentId())) {
                mine.add(toResponse(request));
            }
        }

        // Newest first, purely for display (FIFO uses sequenceNo ASC elsewhere)
        mine.sort((a, b) -> Long.compare(
                b.sequenceNo(), a.sequenceNo()));

        return ResponseEntity.ok(Map.of(
                "items", mine,
                "total", mine.size()
        ));
    }

    // ---------- helpers ----------

    private static String requireStudentId(HttpSession session) {

        if (session == null) {
            throw new IllegalStateException(
                    "No active session");
        }

        Object value = session.getAttribute("AUTHENTICATED_USER_ID");

        if (value == null || value.toString().isBlank()) {
            throw new IllegalStateException(
                    "Not authenticated");
        }

        Object role = session.getAttribute("AUTHENTICATED_USER_ROLE");

        if (role != null && !"STUDENT".equals(role.toString())) {
            throw new IllegalStateException(
                    "Only students can access this endpoint");
        }

        return value.toString();
    }

    private static RegistrationRequestResponse toResponse(
            RegistrationRequest request) {

        return new RegistrationRequestResponse(
                request.getRequestId(),
                request.getStudentId(),
                request.getOfferingId(),
                request.getSequenceNo(),
                request.getStatus().name(),
                request.getRequestedAt()
        );
    }
}