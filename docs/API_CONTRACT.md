# REST API + DTO CONTRACT — FROZEN v0.2

**Base:** `/api/v1`; JSON camelCase; money is a **string decimal** (`"12500.00"`) to avoid JavaScript floating-point mistakes; timestamps UTC `Z`. No request may specify a `studentId` for its own-data endpoints. Student identity comes from the server-side authenticated session. IDs in responses are UUIDv4-prefixed strings. For all state-changing routes, valid CSRF token required for browser session. All `/admin/**` routes require ADMIN.

**Standard output:** singular resource endpoints return the DTO object; GET lists return `{ "items": [...], "total": n }`; POST returns `201` and a resource/decision DTO; normal PATCH returns `200` with updated DTO; logout returns `204`. For input validation error use `400`; unauthenticated `401`, forbidden `403`, not found `404`, duplicate/invalid transition/seat/full/cycle `409`; unexpected TXT I/O `500`. Error body:

```json
{
  "error": {
    "code": "REGISTRATION_CAPACITY_FULL",
    "message": "No seats remain for this offering",
    "timestamp": "2026-10-18T08:15:30Z",
    "path": "/api/v1/students/me/requests"
  }
}
```

## M1 Accounts (`accounts/controller/`)

| HTTP route | Role | Input DTO / body fields | Response DTO essentials |
|---|---|---|---|
| `POST /auth/register` | Public | `RegisterStudentRequest`: `{studentNo,fullName,email,password}` | `201 StudentProfileResponse {studentId,studentNo,fullName,email,status}` |
| `POST /auth/login` | Public | `LoginRequest`: `{email,password}` | `200 {userId,role,fullName}` + HttpOnly session cookie |
| `POST /auth/logout` | Authenticated | no body | `204`, invalidates session |
| `GET /auth/me` | Authenticated | none | `{userId,role,fullName}` |
| `GET /auth/csrf` | Public/session | none | `{headerName,token}` |
| `GET /students/me` | Student | none | `StudentProfileResponse` |
| `PATCH /students/me` | Student | `UpdateStudentRequest`: `{fullName,email}` (at least one) | updated `StudentProfileResponse` |
| `POST /students/me/password` | Student | `ChangePasswordRequest`: `{currentPassword,newPassword}` | `200 {changed:true}` |
| `POST /admin/students` | Admin | `RegisterStudentRequest` | `201 StudentProfileResponse` |
| `GET /admin/students?query=` | Admin | optional search string | list of `StudentProfileResponse` |
| `GET /admin/students/{studentId}` | Admin | path ID | `StudentProfileResponse` |
| `PATCH /admin/students/{studentId}` | Admin | `UpdateStudentRequest` | updated `StudentProfileResponse` |
| `PATCH /admin/students/{studentId}/status` | Admin | `UpdateStudentStatusRequest`: `{status:"ACTIVE"|"INACTIVE"}` | updated `StudentProfileResponse` |

Do not return password hash in DTO. When student is INACTIVE, authentication/registration eligibility fails.

## M2 Courses (`courses/controller/`)

| HTTP route | Role | Input DTO / body fields | Response DTO essentials |
|---|---|---|---|
| `GET /courses?query=&type=` | Student/Admin | query/type filters | published `CourseResponse` list |
| `GET /courses/{courseId}` | Student/Admin | path ID | `{courseId,courseCode,title,description,credits,type,mentoringSessions,status}` |
| `GET /offerings/{offeringId}` | Student/Admin | path ID | `OfferingResponse {offeringId,courseId,termCode,capacity,seatsAvailable,feeAmount,currency,registrationOpensAt,registrationClosesAt,classStartsOn,classEndsOn,status}` |
| `GET /admin/courses` | Admin | none | all courses including drafts |
| `POST /admin/courses` | Admin | `CreateCourseRequest`: `{courseCode,title,description,credits,type,mentoringSessions}` | `201 CourseResponse` (initial DRAFT) |
| `PATCH /admin/courses/{courseId}` | Admin | `UpdateCourseRequest`: any editable course fields | updated `CourseResponse` |
| `PATCH /admin/courses/{courseId}/status` | Admin | `{status:"DRAFT"|"PUBLISHED"|"ARCHIVED"}` | `CourseResponse` |
| `POST /admin/offerings` | Admin | `CreateOfferingRequest`: `{courseId,termCode,capacity,feeAmount,currency,registrationOpensAt,registrationClosesAt,classStartsOn,classEndsOn}` | `201 OfferingResponse` (initial DRAFT) |
| `PATCH /admin/offerings/{offeringId}` | Admin | `UpdateOfferingRequest`: any editable offering fields | `OfferingResponse` |
| `PATCH /admin/offerings/{offeringId}/status` | Admin | `{status:"DRAFT"|"OPEN"|"CLOSED"|"ARCHIVED"}` | `OfferingResponse` |

Reject archiving while live enrollments or other dependencies require availability; do not alter old invoices. M4 calculates effective seats from enrollment port; M2 read DTO obtains a read-only count, never writes `enrollments.txt`.

## M3 Scheduling and prerequisite rules (`scheduling/controller/`)

| HTTP route | Role | Input DTO / body fields | Response DTO essentials |
|---|---|---|---|
| `GET /lecturers/{lecturerId}` | Student/Admin | path ID | `{lecturerId,fullName,department,status}` (no unpublished/private data) |
| `GET /offerings/{offeringId}/schedule` | Student/Admin | path ID | active schedule list `{scheduleId,lecturerId,dayOfWeek,startLocal,endLocal,timeZone,room}` |
| `GET /courses/{courseId}/prerequisites` | Student/Admin | path ID | active rule list `{prerequisiteId,courseId,requiredCourseId}` |
| `GET /students/me/prerequisite-check?offeringId=...` | Student | offering ID | `EligibilityResponse {eligible,missingCourseIds,conflicts}` |
| `GET /students/me/timetable` | Student | none | schedule list for M4-owned live enrollments |
| `GET /admin/lecturers` | Admin | none | all lecturers |
| `POST /admin/lecturers` | Admin | `CreateLecturerRequest`: `{fullName,email,department}` | `201 LecturerResponse` |
| `PATCH /admin/lecturers/{lecturerId}` | Admin | `UpdateLecturerRequest`: `{fullName?,email?,department?,status?}` | `LecturerResponse` |
| `GET /admin/schedules?offeringId=` | Admin | optional filter | all schedules |
| `POST /admin/schedules` | Admin | `CreateScheduleRequest`: `{offeringId,lecturerId,dayOfWeek,startLocal,endLocal,timeZone,room}` | `201 ScheduleResponse` |
| `PATCH /admin/schedules/{scheduleId}` | Admin | `UpdateScheduleRequest`: `{lecturerId?,dayOfWeek?,startLocal?,endLocal?,timeZone?,room?,status?}` | `ScheduleResponse` |
| `GET /admin/prerequisites?courseId=` | Admin | optional filter | all prerequisite rules including INACTIVE |
| `POST /admin/prerequisites` | Admin | `CreatePrerequisiteRequest`: `{courseId,requiredCourseId}` | `201 PrerequisiteResponse` |
| `PATCH /admin/prerequisites/{prerequisiteId}` | Admin | `UpdatePrerequisiteRequest`: `{requiredCourseId?,status?}` | `PrerequisiteResponse` |
| `DELETE /admin/prerequisites/{prerequisiteId}` | Admin | none | `204` if safe to remove; otherwise `409`, use INACTIVE status instead |

All rules validated against active graph for cycles/self edges/duplicates; all session eligibility enforced again by M4, not trusted from eligibility preview.

## M4 Registration and enrollment (`registration/controller/`)

| HTTP route | Role | Input DTO / body fields | Response DTO essentials |
|---|---|---|---|
| `POST /students/me/requests` | Student | `SubmitRegistrationRequest`: `{offeringId}` | `201 RegistrationRequestResponse {requestId,studentId,offeringId,sequenceNo,status,requestedAt}` |
| `GET /students/me/requests` | Student | none | own request-history list |
| `PATCH /students/me/requests/{requestId}/cancel` | Student | no body | request with `status:CANCELLED` (only if PENDING) |
| `GET /students/me/enrollments` | Student | none | own enrollment list, statuses incl. unpaid/active |
| `GET /admin/requests?status=` | Admin | status optional | request queue/history list |
| `POST /admin/requests/next/decision` | Admin | `DecideNextRequestRequest`: `{decision:"APPROVE"|"REJECT",reason?}` | `200 {requestId,status,enrollmentId?,invoiceId?}`; decides oldest PENDING only |
| `GET /admin/offerings/{offeringId}/roster` | Admin | path ID | list of enrollment/student views |
| `PATCH /admin/enrollments/{enrollmentId}/withdraw` | Admin | `WithdrawEnrollmentRequest`: `{reason}` | updated enrollment if legal; no grade/audit violation |

Approval response may include pending invoice ID. `requestId` is not supplied by admin for FIFO decision: server uses `peek()` under lock. Approved paid enrollment transitions to `PENDING_PAYMENT`; free to `ACTIVE`. One 24-hour hold, expired before financial verification. No false physical delete of requests.

## M5 Results, GPA and sort (`results/controller/`)

| HTTP route | Role | Input DTO / body fields | Response DTO essentials |
|---|---|---|---|
| `GET /students/me/results` | Student | none | own PUBLISHED result list `{resultId,courseId,creditsSnapshot,weightedScore,letterGrade,gradePoint,publishedAt}` |
| `GET /students/me/gpa` | Student | none | `GpaResponse {gpa,earnedCredits,attemptedCredits}` |
| `POST /students/me/grade-reviews` | Student | `CreateGradeReviewRequest`: `{resultId,reason}` | `201 {reviewId,resultId,status,submittedAt}` |
| `GET /students/me/grade-reviews` | Student | none | own review list |
| `GET /admin/results?offeringId=` | Admin | optional filter | draft + published result list |
| `POST /admin/results` | Admin | `CreateDraftResultRequest`: `{enrollmentId}` | `201 ResultResponse` (DRAFT) |
| `PATCH /admin/results/{resultId}` | Admin | `UpdateDraftMarksRequest`: `{quizMark,assignmentMark,finalExamMark}` (strings, score 0..100) | updated DRAFT `ResultResponse` |
| `POST /admin/results/{resultId}/publish` | Admin | none | `200 ResultResponse` PUBLISHED and snapshots grade |
| `GET /admin/grade-reviews` | Admin | none | review list |
| `PATCH /admin/grade-reviews/{reviewId}/decision` | Admin | `DecideGradeReviewRequest`: `{decision:"APPROVE"|"REJECT",reviewNote,correctedQuizMark?,correctedAssignmentMark?,correctedFinalExamMark?}` | review; APPROVE requires three valid corrected marks; preserve before/after review values then apply the correction (separate workflow journal not required) |
| `GET /admin/reports/registration-order?offeringId=` | Admin | offering ID | `RegistrationOrderResponse {offeringId,students:[{studentId,requestId,requestedAt,sequenceNo}]}` sorted by manual Insertion Sort |

Sort uses M4 approved registration request timestamp `requestedAt`, not GPA/result creation time. M5 cannot open or write M4 enrollment/registration files; port returns `RegistrationTimeView` approved requests.

## M6 Payments and refunds (`payments/controller/`)

| HTTP route | Role | Input DTO / body fields | Response DTO essentials |
|---|---|---|---|
| `GET /students/me/invoices` | Student | none | own immutable invoice list `{invoiceId,enrollmentId,totalAmount,currency,status,issuedAt}` |
| `GET /students/me/payments` | Student | none | own payment history list |
| `POST /students/me/payments/demo` | Student | `DemoPaymentRequest`: `{invoiceId,idempotencyKey}` | `201 PaymentResponse {paymentId,invoiceId,status,amount,currency}` (VERIFIED if valid); retry existing key returns `200` original |
| `POST /students/me/payments/manual` | Student | `ManualPaymentRequest`: `{invoiceId,externalReference,idempotencyKey}` | `201 PaymentResponse` PENDING_VERIFICATION; retry existing key `200` |
| `GET /students/me/receipts/{paymentId}` | Student | path ID | `ReceiptResponse` only when payment VERIFIED, owned by student |
| `POST /students/me/refunds` | Student | `RefundRequestDto`: `{paymentId,reason}` | `201 RefundResponse` REQUESTED |
| `GET /students/me/refunds` | Student | none | own refund list |
| `GET /admin/invoices` | Admin | optional query filters | invoice list |
| `GET /admin/payments` | Admin | optional query filters | payment list |
| `PATCH /admin/payments/{paymentId}/verification` | Admin | `VerifyPaymentRequest`: `{decision:"VERIFY"|"REJECT",note}` | `PaymentResponse` VERIFIED/REJECTED; VERIFY coordinates M4 activation |
| `GET /admin/payments/report` | Admin | none | `{currency:"LKR",verifiedTotal,outstandingTotal,verifiedCount,pendingCount}` |
| `GET /admin/refunds` | Admin | none | refund list |
| `PATCH /admin/refunds/{refundId}/decision` | Admin | `RefundDecisionRequest`: `{decision:"APPROVE"|"REJECT",note}` | refund APPROVED/REJECTED |
| `POST /admin/refunds/{refundId}/complete` | Admin | none | `200 RefundResponse` COMPLETED; simulated reversal and M4 status action coordinated |
| `PATCH /admin/invoices/{invoiceId}/void` | Admin | `{reason}` | `InvoiceResponse` VOID only when ISSUED and associated enrollment is no longer payable |

No real card data. A client cannot change payment status. Recheck ownership, invoice status, exact amount, hold and duplicate reference under the agreed single-JVM critical section. Demo payment is a local test mode clearly labeled DEMO in both UIs.

## Cross-endpoint example: paid registration

1. `POST /api/v1/students/me/requests` body `{ "offeringId": "OFF_..." }` → `201` `PENDING` request.
2. Admin `POST /api/v1/admin/requests/next/decision` body `{ "decision": "APPROVE" }` → `200` `{ "requestId":"REQ_...","status":"APPROVED","enrollmentId":"ENR_...","invoiceId":"INV_..." }`.
3. Student `GET /api/v1/students/me/invoices` → invoice amount snapshot `"12500.00"` LKR.
4. Student `POST /api/v1/students/me/payments/demo` body `{ "invoiceId":"INV_...", "idempotencyKey":"demo-attempt-001" }` → `201` VERIFIED payment, invoice PAID, M4 enrollment ACTIVE (all server-side).
5. Student `GET /api/v1/students/me/enrollments` and own receipt page → ACTIVE and receipt. Repeating same idempotency key returns existing payment `200`, never double charges/activations.

## Contract-level error examples

- `400 INPUT_VALIDATION_FAILED`: bad date, malformed email or score outside 0..100.
- `401 AUTHENTICATION_REQUIRED`: no login session.
- `403 ACCESS_DENIED`: student attempts `/admin/**` or another student's receipt.
- `404 STUDENT_NOT_FOUND`, `COURSE_NOT_FOUND`, `INVOICE_NOT_FOUND`.
- `409 DUPLICATE_STUDENT`, `PREREQUISITE_NOT_MET`, `PREREQUISITE_CYCLE`, `TIMETABLE_CONFLICT`, `REGISTRATION_CAPACITY_FULL`, `INVALID_STATUS_TRANSITION`, `DUPLICATE_PAYMENT_REFERENCE`, `INVOICE_NOT_PAYABLE`.
- `500 STORAGE_FAILURE`: TXT storage unavailable/corrupt; private path suppressed.

**FROZEN API contract v0.2.** Routes and DTOs are unchanged from v0.1. Only advanced journal/recovery dependency is removed from mandatory implementation. Any endpoint/field change requires a reviewed contract version bump.
