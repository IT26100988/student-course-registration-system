package com.example.studentregistration.registration.service;

import com.example.studentregistration.registration.model.Enrollment;
import com.example.studentregistration.registration.model.EnrollmentStatus;
import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.model.RequestStatus;
import com.example.studentregistration.registration.repository.txt.TxtEnrollmentRepository;
import com.example.studentregistration.registration.repository.txt.TxtRegistrationRequestRepository;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class EnrollmentLifecycleServiceTest {

    private static final String ADMIN_ID =
            "ADM_aaaaaaaa-aaaa-4aaa-8aaa-aaaaaaaaaaaa";

    @Test
    void approvePaidCreatesPendingPaymentEnrollment() throws Exception {

        Path dir = Files.createTempDirectory("m4-test-");

        TxtRegistrationRequestRepository reqRepo =
                new TxtRegistrationRequestRepository(dir.toString());
        TxtEnrollmentRepository enrRepo =
                new TxtEnrollmentRepository(dir.toString());

        EnrollmentLifecycleService life =
                new EnrollmentLifecycleService(reqRepo, enrRepo);

        RegistrationRequest request = new RegistrationRequest(
                "REQ_11111111-1111-4111-8111-111111111111",
                "STU_22222222-2222-4222-8222-222222222222",
                "OFF_33333333-3333-4333-8333-333333333333",
                1L,
                RequestStatus.PENDING,
                Instant.parse("2026-11-01T08:00:00Z")
        );
        reqRepo.save(request);

        RegistrationRequest approved = life.approveRequest(
                request.getRequestId(), ADMIN_ID, "Approved", true);

        assertEquals(RequestStatus.APPROVED, approved.getStatus());
        assertEquals(1, enrRepo.findAll().size());

        Enrollment enrollment = enrRepo.findAll().get(0);
        assertEquals(EnrollmentStatus.PENDING_PAYMENT, enrollment.getStatus());
        assertNotNull(enrollment.getHoldExpiresAt());
    }

    @Test
    void approveFreeCreatesActiveEnrollment() throws Exception {

        Path dir = Files.createTempDirectory("m4-test-");

        TxtRegistrationRequestRepository reqRepo =
                new TxtRegistrationRequestRepository(dir.toString());
        TxtEnrollmentRepository enrRepo =
                new TxtEnrollmentRepository(dir.toString());

        EnrollmentLifecycleService life =
                new EnrollmentLifecycleService(reqRepo, enrRepo);

        RegistrationRequest request = new RegistrationRequest(
                "REQ_44444444-4444-4444-8444-444444444444",
                "STU_22222222-2222-4222-8222-222222222222",
                "OFF_55555555-5555-4555-8555-555555555555",
                1L,
                RequestStatus.PENDING,
                Instant.parse("2026-11-01T09:00:00Z")
        );
        reqRepo.save(request);

        life.approveRequest(
                request.getRequestId(), ADMIN_ID, "Free", false);

        Enrollment enrollment = enrRepo.findAll().get(0);
        assertEquals(EnrollmentStatus.ACTIVE, enrollment.getStatus());
        assertNull(enrollment.getHoldExpiresAt());
    }

    @Test
    void duplicateApproveIsBlocked() throws Exception {

        Path dir = Files.createTempDirectory("m4-test-");

        TxtRegistrationRequestRepository reqRepo =
                new TxtRegistrationRequestRepository(dir.toString());
        TxtEnrollmentRepository enrRepo =
                new TxtEnrollmentRepository(dir.toString());

        EnrollmentLifecycleService life =
                new EnrollmentLifecycleService(reqRepo, enrRepo);

        RegistrationRequest request = new RegistrationRequest(
                "REQ_11111111-1111-4111-8111-111111111111",
                "STU_22222222-2222-4222-8222-222222222222",
                "OFF_33333333-3333-4333-8333-333333333333",
                1L,
                RequestStatus.PENDING,
                Instant.parse("2026-11-01T08:00:00Z")
        );
        reqRepo.save(request);

        life.approveRequest(
                request.getRequestId(), ADMIN_ID, "First", true);

        Exception ex = assertThrows(Exception.class, () ->
                life.approveRequest(
                        request.getRequestId(), ADMIN_ID, "Again", true));

        assertTrue(ex.getMessage().contains("PENDING"));
    }

    @Test
    void activateAfterPaymentSucceeds() throws Exception {

        Path dir = Files.createTempDirectory("m4-test-");

        TxtRegistrationRequestRepository reqRepo =
                new TxtRegistrationRequestRepository(dir.toString());
        TxtEnrollmentRepository enrRepo =
                new TxtEnrollmentRepository(dir.toString());

        EnrollmentLifecycleService life =
                new EnrollmentLifecycleService(reqRepo, enrRepo);

        RegistrationRequest request = new RegistrationRequest(
                "REQ_11111111-1111-4111-8111-111111111111",
                "STU_22222222-2222-4222-8222-222222222222",
                "OFF_33333333-3333-4333-8333-333333333333",
                1L,
                RequestStatus.PENDING,
                Instant.parse("2026-11-01T08:00:00Z")
        );
        reqRepo.save(request);

        life.approveRequest(
                request.getRequestId(), ADMIN_ID, "Paid", true);

        Enrollment pending = enrRepo.findAll().get(0);
        Enrollment activated = life.activateAfterPayment(
                pending.getEnrollmentId());

        assertEquals(EnrollmentStatus.ACTIVE, activated.getStatus());
        assertNotNull(activated.getActivatedAt());
    }

    @Test
    void withdrawActiveEnrollmentSucceeds() throws Exception {

        Path dir = Files.createTempDirectory("m4-test-");

        TxtRegistrationRequestRepository reqRepo =
                new TxtRegistrationRequestRepository(dir.toString());
        TxtEnrollmentRepository enrRepo =
                new TxtEnrollmentRepository(dir.toString());

        EnrollmentLifecycleService life =
                new EnrollmentLifecycleService(reqRepo, enrRepo);

        RegistrationRequest request = new RegistrationRequest(
                "REQ_44444444-4444-4444-8444-444444444444",
                "STU_22222222-2222-4222-8222-222222222222",
                "OFF_55555555-5555-4555-8555-555555555555",
                1L,
                RequestStatus.PENDING,
                Instant.parse("2026-11-01T09:00:00Z")
        );
        reqRepo.save(request);

        life.approveRequest(
                request.getRequestId(), ADMIN_ID, "Free", false);

        Enrollment active = enrRepo.findAll().get(0);
        Enrollment withdrawn = life.withdrawEnrollment(
                active.getEnrollmentId(), ADMIN_ID, "Medical");

        assertEquals(EnrollmentStatus.WITHDRAWN, withdrawn.getStatus());
        assertNotNull(withdrawn.getClosedAt());
    }

    @Test
    void cancelByWrongStudentIsBlocked() throws Exception {

        Path dir = Files.createTempDirectory("m4-test-");

        TxtRegistrationRequestRepository reqRepo =
                new TxtRegistrationRequestRepository(dir.toString());
        TxtEnrollmentRepository enrRepo =
                new TxtEnrollmentRepository(dir.toString());

        EnrollmentLifecycleService life =
                new EnrollmentLifecycleService(reqRepo, enrRepo);

        RegistrationRequest request = new RegistrationRequest(
                "REQ_66666666-6666-4666-8666-666666666666",
                "STU_77777777-7777-4777-8777-777777777777",
                "OFF_88888888-8888-4888-8888-888888888888",
                1L,
                RequestStatus.PENDING,
                Instant.parse("2026-11-01T10:00:00Z")
        );
        reqRepo.save(request);

        Exception ex = assertThrows(Exception.class, () ->
                life.cancelByStudent(
                        request.getRequestId(),
                        "STU_22222222-2222-4222-8222-222222222222"));

        assertTrue(ex.getMessage().contains("own"));
    }

    @Test
    void rejectWithReasonSucceeds() throws Exception {

        Path dir = Files.createTempDirectory("m4-test-");

        TxtRegistrationRequestRepository reqRepo =
                new TxtRegistrationRequestRepository(dir.toString());
        TxtEnrollmentRepository enrRepo =
                new TxtEnrollmentRepository(dir.toString());

        EnrollmentLifecycleService life =
                new EnrollmentLifecycleService(reqRepo, enrRepo);

        RegistrationRequest request = new RegistrationRequest(
                "REQ_99999999-9999-4999-8999-999999999999",
                "STU_22222222-2222-4222-8222-222222222222",
                "OFF_33333333-3333-4333-8333-333333333333",
                1L,
                RequestStatus.PENDING,
                Instant.parse("2026-11-01T08:00:00Z")
        );
        reqRepo.save(request);

        RegistrationRequest rejected = life.rejectRequest(
                request.getRequestId(), ADMIN_ID, "No prerequisites");

        assertEquals(RequestStatus.REJECTED, rejected.getStatus());
        assertEquals("No prerequisites", rejected.getDecisionReason());
        assertEquals(0, enrRepo.findAll().size());
    }
}