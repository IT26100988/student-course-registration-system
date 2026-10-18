# FROZEN TEAM CONTRACT — v0.2 (SIMPLIFIED STUDENT PROJECT)

**System:** Student Course Registration and Payment System  
**Status:** APPROVED v0.2 simplification by Team Leader on 2026-10-18; local project has been initialized through the shared infrastructure lessons; GitHub baseline not yet published.  
**Prompt basis:** `FINAL_MASTER_PROMPT.txt`, version 3.0.  
**Rule:** v0.2 supersedes v0.1 for scope and reliability obligations. Other approved models, routes, DTOs, enum values, schemas, ownership and branching remain unchanged. Future contract changes require explicit leader approval, a version bump and notification to all six members (a pull request once GitHub exists). Read `SCOPE_V0.2.md` first.

## 1. One system, two roles, six owners

- One Java Spring Boot REST/Maven application; one GitHub repository; one deployed single-JVM backend; HTML/CSS/vanilla JavaScript pages for STUDENT and ADMIN.
- Only UTF-8 plain TXT data under project-root `data/`, via `BufferedReader`/`BufferedWriter`; no database, JPA, JDBC, Hibernate or object serialization.
- The chain in every module is **POJO → repository interface → TXT repository implementation → service → controller → JavaScript fetch()**.
- Every owner supplies C/R/U as distinct workflows, meaningful `ArrayList<T>` usage, custom exception, negative tests, Student and Admin pages, and OOP relation.
- Exactly two account roles: `STUDENT` and `ADMIN`. A `SYSTEM` audit actor (if used) is not a third login role.
- `main` is protected by team policy, members work in permanent feature branches, and only reviewed pull requests merge to `main`.

## 2. Frozen identities and project-wide conventions

- Base Java package: `com.example.studentregistration`. Prefer feature-root subpackages (`accounts`, `courses`, `scheduling`, `registration`, `results`, `payments`) plus `shared`.
- Artifact and repository folder: `student-course-registration`. Root Spring class: `com.example.studentregistration.StudentRegistrationApplication`.
- Files live at `${app.storage.dir:data}` (relative to working directory by default), not in `static` and not inside `src/main/resources`.
- IDs: prefixed random **UUIDv4**, e.g. `STU_11111111-1111-4111-8111-111111111111`. Never reuse; never use `ArrayList` index for IDs. Prefixes: `STU`, `ADM`, `CRS`, `OFF`, `LEC`, `SCH`, `PRE`, `REQ`, `ENR`, `RES`, `REV`, `INV`, `LIN`, `PAY`, `RFD`, `TXN`, `AUD`.
- Monetary amounts use Java `BigDecimal` (`scale=2`), invariant `LKR` currency; do not use double/float for money. Reject unsupported currency. An invoice snapshots fee at issue time; course-fee edits cannot reprice it.
- All timestamps are `Instant` serialized UTC ISO-8601, e.g. `2026-10-18T08:15:30Z`; recurring weekly schedule times use local `HH:mm`, `DayOfWeek`, and IANA zone `Asia/Colombo`. Date-only fields use `YYYY-MM-DD`.
- TXT scheme and exactly one writer per file are defined in `TXT_SCHEMA.md`. Read-only access to another module's data must go through its **service/port**, never its concrete repository or TXT file.
- All user-facing JSON is camelCase. One error shape: `{ "error": { "code": "STUDENT_NOT_FOUND", "message": "Student not found", "timestamp": "2026-10-18T08:15:30Z", "path": "/api/v1/..." } }`.
- HTTP: 200 successful GET/PATCH/decision; 201 successful POST/create; 204 successful logout; 400 malformed/invalid, 401 no session, 403 wrong role/ownership, 404 missing resource, 409 state conflict/duplicate/seat exhaustion, 500 unexpected storage failure (sanitized error message); no raw stack traces or file paths sent to client.
- Normal GET collection response: `{ "items": [ ... ], "total": 3 }`; pagination optional for this educational project. Single GET returns one DTO directly; successful POST returns a DTO including the generated ID.

## 3. Security and authorization

- BCrypt hashed passwords in `students.txt`/`admins.txt`, never plaintext. Server-side session cookie: HttpOnly, SameSite=Lax, Secure in HTTPS; Spring Security route guards with CSRF enabled for browser writes. `fetch()` sends same-origin credentials plus CSRF token header for POST/PATCH/DELETE.
- `POST /api/v1/auth/register` and login are public; student `/students/me/**` derives the studentId from the session, never trusts a body-supplied studentId. `/api/v1/admin/**` requires ADMIN, not merely hidden navigation.
- Create first ADMIN using a documented **one-time environment-configured bootstrap** when no admin exists. No default hardcoded password and never commit bootstrap secrets.
- Logs, sensitive data, temp/journal files and backups are excluded from Git; only `data/.gitkeep`, sanitized fake fixtures and documentation are committed.

## 4. Business workflow and status policy

1. M1 creates/activates student login; M2 creates/publishes course and OPEN offering with capacity, fee and registration window; M3 configures schedules, lecturers, and optional prerequisite edges.
2. Student requests a specific `offeringId` through M4. M4 validates ACTIVE student, published course and open offering/window, available effective seats, no duplicate live request/enrollment, timetable conflicts and **every M3 prerequisite must have an M5 PUBLISHED passing result**. On success persist `PENDING` request with immutable monotone `sequenceNo`, then enqueue in `ArrayDeque` implementing `Queue`.
3. M4 admin can decide only the oldest still-pending request. On approval revalidate capacity and eligibility **under a shared, single-JVM critical section**, set APPROVED, create `PENDING_PAYMENT` enrollment and reserve a seat, then ask M6 to issue a single, fee-snapshot invoice. In this educational version, use one application-instance critical section and check for errors. Locks alone do not make multiple TXT writes atomic. If fee is `0.00`, make enrollment ACTIVE without invoice/payment.
4. M6 demo verification is explicitly **DEMO** and server verified; manual bank-transfer reference is PENDING_VERIFICATION until ADMIN verifies it. On valid paid amount and hold still valid, M6 asks M4-owned `EnrollmentPaymentPort` to make enrollment ACTIVE. Receipt generated from verified payment data. No real payment gateway, cards, or automatic bank validation.
5. An unpaid enrollment expires after a proposed **24-hour hold**; it changes to EXPIRED, releases capacity and its unpaid invoice becomes VOID. Sweep expired holds before seat/payment operations and at startup. Never treat a client-supplied `paid=true` as proof.
6. M5 may create a result only for an ACTIVE enrollment. Quiz=20%, Assignment=30%, Final exam=50%; three score values in [0,100] must exist before PUBLISHED. Credit-weighted GPA uses only published official results, including published failures; passing is weighted score >= 50.00. Results are student-private until published.
7. A published result can be corrected only from an APPROVED grade review; `grade_reviews.txt` captures original and corrected mark snapshots, reviewer and reason. Do not silently overwrite without a preserved review record.
8. M5 produces manual stable ascending **Insertion Sort** using immutable `RegistrationRequest.requestedAt`, with `sequenceNo` as tie-breaker; no `Collections.sort`, `Arrays.sort` or stream `.sorted()` in that algorithm.

**Request states:** `PENDING → APPROVED | REJECTED | CANCELLED` (terminal).  
**Enrollment states:** `PENDING_PAYMENT → ACTIVE | EXPIRED | CANCELLED`; `ACTIVE → WITHDRAWN` only by an authorized policy; terminal statuses retain history.  
**Invoice states:** `ISSUED → PAID | VOID`; `PAID → REFUNDED` only after a completed full DEMO refund.  
**Payment states:** `PENDING_VERIFICATION → VERIFIED | REJECTED`; demo payment may be created VERIFIED atomically; failed demo attempts are `FAILED` and cannot activate enrollment.  
**Refund states:** `REQUESTED → APPROVED | REJECTED`; `APPROVED → COMPLETED`; refund completion is simulated, not an actual fund transfer.  
**Result states:** `DRAFT → PUBLISHED`.  
**Grade review states:** `PENDING → APPROVED | REJECTED`; `APPROVED → APPLIED` only after the correction is saved with its review history.

**Seat rule:** Count enrollment statuses `PENDING_PAYMENT` and `ACTIVE` against offering capacity; request PENDING does not consume a seat. Re-check under the shared coordinator before approving the next request. Free enrollment becomes ACTIVE at approval. Effective seats are derived, not directly decremented in M2 files.

**Prerequisite rule:** An ACTIVE rule `CRS_A requires CRS_B` means the student must already have an M5 `PUBLISHED` passing result for course `CRS_B`. No self-edge, duplicate edge or directed cycle. No prerequisite requirement for a course whose rule list is empty. Initial demo fixtures can include historical *fake* ACTIVE enrollments plus published results; there is no hidden SQL data.

## 5. Cross-module ports (owned under `shared/port` by the leader)

These signatures describe the stable public boundary; concrete classes and DTOs will be created **only after approval**. Ports are interfaces, implemented by the indicated member. Exceptions are described as business exceptions and translated by the global handler.

| Port and owner of implementation | Method signature | Consumer |
|---|---|---|
| `StudentAccountPort` — M1 | `StudentEligibilityView requireActiveStudent(String studentId)` | M4, M6, M5 |
| `OfferingCatalogPort` — M2 | `OfferingSnapshot requirePublishedOffering(String offeringId)` | M3, M4, M6, M5 |
| `OfferingCatalogPort` — M2 | `CourseSnapshot requireCourse(String courseId)` | M3, M5 |
| `SchedulingEligibilityPort` — M3 | `void assertEligible(String studentId, String offeringId)` | M4 |
| `EnrollmentReadPort` — M4 | `List<EnrollmentView> findStudentEnrollments(String studentId)` | M3, M5 |
| `EnrollmentReadPort` — M4 | `EnrollmentView requireActive(String enrollmentId, String studentId)` | M5 |
| `EnrollmentReadPort` — M4 | `int countSeatsTaken(String offeringId)` | M4 approval service |
| `PassedCoursePort` — M5 | `boolean hasPublishedPass(String studentId, String courseId)` | M3 |
| `InvoiceIssuePort` — M6 | `InvoiceView issueOnce(String enrollmentId, String studentId, String offeringId, BigDecimal amount, String currency)` | M4 |
| `InvoiceIssuePort` — M6 | `void voidUnpaidForEnrollment(String enrollmentId)` | M4 expiry |
| `EnrollmentPaymentPort` — M4 | `EnrollmentView activateAfterVerifiedPayment(String enrollmentId, String invoiceId, String paymentId)` | M6 |
| `RegistrationTimestampPort` — M4 | `List<RegistrationTimeView> listByOffering(String offeringId)` | M5 sorting |

Stable read-only DTO fields:
- `StudentEligibilityView(studentId: String, active: boolean)`.
- `CourseSnapshot(courseId: String, code: String, credits: int, published: boolean)`.
- `OfferingSnapshot(offeringId: String, courseId: String, capacity: int, feeAmount: BigDecimal, currency: String, opensAt: Instant, closesAt: Instant, classStartsOn: LocalDate, classEndsOn: LocalDate, open: boolean)`.
- `EnrollmentView(enrollmentId: String, requestId: String, studentId: String, offeringId: String, status: EnrollmentStatus, createdAt: Instant)`.
- `InvoiceView(invoiceId: String, enrollmentId: String, totalAmount: BigDecimal, currency: String, status: InvoiceStatus)`.
- `RegistrationTimeView(requestId: String, studentId: String, offeringId: String, requestedAt: Instant, sequenceNo: long)`.

Port interfaces and DTOs are **leader-owned shared contracts**. Members may implement their interfaces but may not redefine these types or rename their methods unilaterally. A temporary stub/mock is allowed in member tests; it must be removed or replaced for merged integration.

## 6. Simplified TXT persistence and operational limits (v0.2)

**Required for the student project:**
- Keep `TxtCodec`, `TxtFileStore`, `FileStorageException`, a shared `StorageLockManager` (or basic `synchronized` critical sections applied consistently), and a shared `GlobalExceptionHandler`.
- Each member must use `BufferedReader`/`BufferedWriter`, UTF-8, a real `ArrayList` read/parse/modify/save workflow, a repository interface + TXT implementation, and unique field/ID validation. The current temp-file rewrite and schema-header validation may be reused as-is.
- Retain single-writer file ownership. Coordinate the M4/M6 registration-to-invoice-to-payment path in one JVM using the existing `SharedWorkflowCoordinator` if helpful, or a clearly agreed single shared critical section. Keep demo payments backend verified and idempotent; keep last-seat, duplicate and ownership checks.
- Preserve requests/payments/refunds/results through status changes rather than silently deleting historic transactions. Test normal restart persistence, FIFO rebuild, invalid input, last seat, and payment retry.

**Optional / not graded scope unless the lecturer later requests it:** `workflow_journal.txt`, `audit_log.txt`, `WorkflowJournalStore`, `WorkflowRecoveryService`, complex operation-specific journal payload/stage validation, automatic rollback/compensation, cross-file startup replay and multi-process concurrency. Previously created educational classes may remain in source if they compile, but member modules must not depend on them or block progress to implement them. Do not claim they provide working recovery.

**When an unexpected cross-file write fails:** stop the operation, return a controlled error and preserve recorded facts. For the project demo, document and manually reconcile any partial state before retrying; where implemented, add a simple consistency-check/report. Never mark an invoice PAID or an enrollment ACTIVE based only on a browser value. This is a best-effort student demonstration, **not crash-atomic or production-grade financial processing**. A real system would require database transactions and approved payment infrastructure.

## 7. UI integration and Git ownership

- Student shell: `src/main/resources/static/student/index.html`; Admin shell: `.../admin/index.html`. Leader owns shell, shared `.../css/common.css` and `.../js/api.js`; members own their own linked pages and scripts listed in `CLASS_OWNERSHIP.md`.
- Branches: M1 `feature/accounts`, M2 `feature/courses`, M3 `feature/lecturers-prerequisites`, M4 `feature/registration-enrollment`, M5 `feature/results-sorting`, M6 `feature/payments`.
- Baseline first (leader creates and pushes exactly one Spring Initializr project); then all six clone the same real GitHub URL. No member initializes another project. Everyone runs tests locally and opens PRs. Integration order: M1 + M2 → M3 → M4 → M6 → M5; parallel coding against approved ports is allowed.
- Do not merge any PR with route, DTO, TXT schema or cross-port breaks without a coordinated `CONTRACT.md` update.

## 8. Approved team decisions

- [x] Exactly six fixed feature owners and every Student/Admin page accepted
- [x] `StandardCourse`/`PremiumCourse` behaviors and one-offering/one-invoice-line policy accepted
- [x] `LKR`, 24-hour hold, session+CSRF, `>=50.00` pass policy and grade weights approved
- [x] Exact field order/encoding and ID prefixes in `TXT_SCHEMA.md` approved
- [x] Exact routes and request DTO shapes in `API_CONTRACT.md` approved
- [x] Shared Java port names/method signatures and sole-writer policy approved
- [x] Status transitions and safe retry rules retained; complex journaling and automatic crash recovery explicitly removed from the mandatory scope by v0.2
- [x] Owner paths and branches in `CLASS_OWNERSHIP.md`/`TEAM_TASKS.md` approved

**Current state: FROZEN v0.2.** The Team Leader approved the simplified student-project scope on 2026-10-18. Next: verify the existing application builds, create/publish ONE GitHub baseline, and distribute the six approved feature branches. Work one small step at a time.
