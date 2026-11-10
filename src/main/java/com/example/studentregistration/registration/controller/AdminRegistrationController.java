package com.example.studentregistration.registration.controller;

import com.example.studentregistration.registration.dto.DecideNextRequestRequest;
import com.example.studentregistration.registration.dto.RegistrationRequestResponse;
import com.example.studentregistration.registration.exception.QueueEmptyException;
import com.example.studentregistration.registration.exception.RegistrationNotAllowedException;
import com.example.studentregistration.registration.model.Enrollment;
import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.service.EnrollmentLifecycleService;
import com.example.studentregistration.registration.service.RegistrationRequestService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin-only FIFO queue processing and listing.
 * Base: /api/v1/admin
 *
 * Protected by SecurityConfig's hasRole("ADMIN") for /api/v1/admin/**.
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminRegistrationController {

    private final RegistrationRequestService requestService;
    private final EnrollmentLifecycleService lifecycleService;

    public AdminRegistrationController(
            RegistrationRequestService requestService,
            EnrollmentLifecycleService lifecycleService) {
        this.requestService = requestService;
        this.lifecycleService = lifecycleService;
    }

    /**
     * GET /api/v1/admin/requests?status=
     * Returns all requests (optionally filtered by status), ordered by sequenceNo ASC.
     */
    @GetMapping("/requests")
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(required = false) String status) {

        ArrayList<RegistrationRequest> requests =
                requestService.listForAdmin(status);

        List<RegistrationRequestResponse> items = new ArrayList<>();
        for (RegistrationRequest request : requests) {
            items.add(toResponse(request));
        }

        return ResponseEntity.ok(Map.of(
                "items", items,
                "total", items.size()
        ));
    }

    /**
     * POST /api/v1/admin/requests/next/decision
     * Decides on the OLDEST PENDING request (FIFO — server peek under lock).
     *
     * Body: { "decision": "APPROVE" | "REJECT", "reason": "...", "isPaid": true|false }
     */
    @PostMapping("/requests/next/decision")
    public ResponseEntity<Map<String, Object>> decideNext(
            @RequestBody DecideNextRequestRequest body,
            HttpSession session) {

        String adminId = requireAdminId(session);

        if (body == null || body.getDecision() == null
                || body.getDecision().isBlank()) {
            throw new IllegalArgumentException(
                    "decision is required (APPROVE or REJECT)");
        }

        RegistrationRequest next = requestService.peekNextPending();

        if (next == null) {
            throw new QueueEmptyException(
                    "No PENDING registration requests in the FIFO queue.");
        }

        String decision = body.getDecision().trim().toUpperCase();

        if ("APPROVE".equals(decision)) {
            boolean isPaid = Boolean.TRUE.equals(body.getIsPaid());

            RegistrationRequest approved =
                    lifecycleService.approveRequest(
                            next.getRequestId(),
                            adminId,
                            body.getReason(),
                            isPaid
                    );

            // FIFO: remove from queue head AFTER successful approval
            requestService.pollNextPending();

            // Find the new enrollment for this request
            String enrollmentId = findEnrollmentIdForRequest(
                    requestService, next.getRequestId());

            return ResponseEntity.ok(Map.of(
                    "requestId", approved.getRequestId(),
                    "status", approved.getStatus().name(),
                    "enrollmentId", enrollmentId == null ? "" : enrollmentId
            ));

        } else if ("REJECT".equals(decision)) {
            RegistrationRequest rejected =
                    lifecycleService.rejectRequest(
                            next.getRequestId(),
                            adminId,
                            body.getReason()
                    );

            requestService.pollNextPending();

            return ResponseEntity.ok(Map.of(
                    "requestId", rejected.getRequestId(),
                    "status", rejected.getStatus().name(),
                    "enrollmentId", ""
            ));
        }

        throw new RegistrationNotAllowedException(
                "Unknown decision: " + body.getDecision()
                        + " (must be APPROVE or REJECT)");
    }

    // ---------- helpers ----------

    private static String requireAdminId(HttpSession session) {

        if (session == null) {
            throw new IllegalStateException("No active session");
        }

        Object role = session.getAttribute("AUTHENTICATED_USER_ROLE");

        if (role == null || !"ADMIN".equals(role.toString())) {
            throw new IllegalStateException(
                    "Only admins can process the FIFO queue");
        }

        Object adminId = session.getAttribute("AUTHENTICATED_USER_ID");

        if (adminId == null || adminId.toString().isBlank()) {
            throw new IllegalStateException(
                    "Authenticated admin ID not found in session");
        }

        return adminId.toString();
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

    /**
     * Helper: find the enrollment ID created for a given requestId.
     * Uses lifecycle's enrollment repository via a new small query.
     */
    private String findEnrollmentIdForRequest(
            RegistrationRequestService service,
            String requestId) {

        // Re-use the repository through lifecycle service if possible.
        // For simplicity, we access it via a small helper method to be added.
        return service.findEnrollmentIdForRequest(requestId);
    }
}