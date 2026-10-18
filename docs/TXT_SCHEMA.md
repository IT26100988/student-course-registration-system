# TXT SCHEMA — FROZEN v0.2

These are the approved exact on-disk contracts. All real data is created at runtime; **never commit `data/*.txt`**. v0.2 preserves all M1–M6 field orders and removes *mandatory* complex journal/recovery implementation; see `SCOPE_V0.2.md`.

## Storage location, encoding and parser contract

- Project-root `data/` by default, configurable `app.storage.dir`; UTF-8, one logical record per physical line.
- First line of **each** TXT file: `#SCHEMA|<file_stem>|1` where `<file_stem>` is the exact filename without `.txt`. For example `#SCHEMA|students|1`. The following lines contain records in the exact field order below, with no secondary headers.
- Pipe `|` is the only outer field separator; `String.split("\\|", -1)` retains trailing empty fields. Each field's *whole raw string* is encoded with standard Java `URLEncoder.encode(value, StandardCharsets.UTF_8)` on write and decoded with `URLDecoder.decode(field, StandardCharsets.UTF_8)` on read. Example `Nimal Perera` → `Nimal+Perera`, `a|b` → `a%7Cb`, a newline → `%0A`, a literal `+` → `%2B`, percent → `%25`. No unescaped embedded pipes/newlines are allowed.
- All non-null values become strings for writing (including enum names, decimal amounts, dates, timestamps). **Optional absent values are encoded as empty fields**, and parsed as null. Mandatory values cannot be blank. Since null and empty string are equivalent for optional fields, no separate sentinel is used. Trim/validate mandatory human-entered fields before encoding; do not trim password hashes. Validate field count and types before using a row.
- `Instant` timestamps use UTC `Z`, `LocalDate` uses ISO `YYYY-MM-DD`, local class-time uses 24-hour `HH:mm`, days are `MONDAY`...`SUNDAY`, currency always `LKR`. Money values are decimal strings with exactly two fractional places, e.g. `12000.00`.
- UUIDs: `<PREFIX>_<canonical-lowercase-UUIDv4>` (e.g. `STU_11111111-1111-4111-8111-111111111111`). Never recycle IDs. `sequenceNo` is a per-system positive **long** allocated under the agreed in-JVM critical section and initialized as `max(sequenceNo) + 1` after restart. Requests are never physically deleted, preserving continuity.
- At first run, create a missing directory and files with their header. If a file exists but has wrong/missing header, wrong field count, bad UTF-8/encoding/enum/date/number or malformed record, fail with a controlled checked `FileStorageException`; DO NOT silently overwrite or ignore it.
- For updates: load into an `ArrayList`, validate, write full contents to a temp file in same directory with UTF-8 `BufferedWriter`, flush, and `Files.move(ATOMIC_MOVE, REPLACE_EXISTING)` when supported; use the existing documented replace fallback if not (fallback is not crash-atomic). For reads: `BufferedReader.readLine()` using UTF-8. Journal/audit files are optional in v0.2; never build persistence from an in-memory-only ArrayList.
- Cross-module reads use immutable port DTOs. Only the named M1–M6 owner can write a business data file. Optional leader-owned journal/audit files must never be independently written by member modules.

## Exact file layouts

The sequence shown in each code span is **literal record field order**. `?` marks a nullable field and is **NOT** a character written to file. `createdAt`, `updatedAt`, identifiers and enumerated required fields cannot be blank.

### M1 — Student Accounts & Authentication

**`data/students.txt` — sole writer M1**  
`studentId | studentNo | fullName | email | passwordHash | status | createdAt | updatedAt`  
`studentNo` unique; email unique across students and admins (case-folded). `status`: `ACTIVE`, `INACTIVE`. `passwordHash` holds BCrypt string; never plaintext.

**`data/admins.txt` — sole writer M1**  
`adminId | fullName | email | passwordHash | status | createdAt | updatedAt`  
Admin entries are created only by an authorized one-time bootstrap/admin process. `status`: `ACTIVE`, `INACTIVE`.

### M2 — Courses, Offerings and Fees

**`data/courses.txt` — sole writer M2**  
`courseId | courseCode | title | description | credits | type | mentoringSessions | status | createdAt | updatedAt`  
`credits` positive int; `type`: `STANDARD`, `PREMIUM`; `mentoringSessions` is `0` for STANDARD and strictly positive for PREMIUM. `status`: `DRAFT`, `PUBLISHED`, `ARCHIVED`. Course codes unique. `Course` is abstract with `StandardCourse`/`PremiumCourse` subclasses whose mentoring validation is real behavior.

**`data/course_offerings.txt` — sole writer M2**  
`offeringId | courseId | termCode | capacity | feeAmount | currency | registrationOpensAt | registrationClosesAt | classStartsOn | classEndsOn | status | createdAt | updatedAt`  
`capacity` positive integer; money nonnegative with 2 decimals, `currency=LKR`; registration opens before closes; class start <= end. `status`: `DRAFT`, `OPEN`, `CLOSED`, `ARCHIVED`; OPEN alone is not enough: current time must be inside registration window and linked course must be PUBLISHED. `termCode` e.g. `2026-S2`; an offering belongs to one course. Offering fee changes affect only invoices issued **later**.

### M3 — Lecturers, Timetable and Prerequisites

**`data/lecturers.txt` — sole writer M3**  
`lecturerId | fullName | email | department | status | createdAt | updatedAt`  
`status`: `ACTIVE`, `INACTIVE`; emails unique among lecturers.

**`data/course_schedules.txt` — sole writer M3**  
`scheduleId | offeringId | lecturerId | dayOfWeek | startLocal | endLocal | timeZone | room | status | createdAt | updatedAt`  
`status`: `ACTIVE`, `INACTIVE`; `timeZone=Asia/Colombo` for v1; startLocal < endLocal (no overnight classes). Effective date interval is inherited from the offering's classStartsOn/classEndsOn. Reject room, lecturer and student recurring-timetable conflicts for overlapping class dates.

**`data/prerequisites.txt` — sole writer M3**  
`prerequisiteId | courseId | requiredCourseId | status | createdAt | updatedAt`  
`status`: `ACTIVE`, `INACTIVE`. Every ACTIVE directed edge means courseId requires a **published PASS** in requiredCourseId. Reject self-reference, duplicate ACTIVE pair and any new edge creating a cycle. M3 reads official passing state only from M5 `PassedCoursePort`.

### M4 — Registration Requests and Enrollments

**`data/registration_requests.txt` — sole writer M4**  
`requestId | studentId | offeringId | sequenceNo | status | requestedAt | processedAt? | processedByAdminId? | decisionReason? | cancelledAt?`  
`status`: `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`. `sequenceNo` immutable positive long, unique and monotone. FIFO rebuild reads PENDING rows, orders by `sequenceNo` ASC, then inserts each into `Queue<RegistrationRequest>`; tie/duplicate sequence is corruption.

**`data/enrollments.txt` — sole writer M4**  
`enrollmentId | requestId | studentId | offeringId | status | createdAt | holdExpiresAt? | activatedAt? | closedAt? | updatedAt`  
`status`: `PENDING_PAYMENT`, `ACTIVE`, `EXPIRED`, `CANCELLED`, `WITHDRAWN`. One enrollment maximum per approved request, identified by unique `requestId`. `holdExpiresAt` required for paid PENDING_PAYMENT, blank for free immediate ACTIVE. Seat counts include PENDING_PAYMENT and ACTIVE, not other statuses. This file is never written by M5 or M6.

### M5 — Results, GPA, Grade Reviews and Manual Sort

**`data/results.txt` — sole writer M5**  
`resultId | enrollmentId | studentId | courseId | creditsSnapshot | quizMark? | assignmentMark? | finalExamMark? | weightedScore? | letterGrade? | gradePoint? | status | createdAt | publishedAt? | updatedAt`  
`status`: `DRAFT`, `PUBLISHED`. Quiz 20%, assignment 30%, final 50%; values 0..100 with two decimals and all required before publish. Weighted score and grade points calculated server-side, not blindly accepted from browser. Grade scheme (proposed): A=85..100 → 4.00, B=70..84.99 → 3.00, C=50..69.99 → 2.00, F=<50 → 0.00. PASS iff PUBLISHED and weightedScore >= 50.00. `creditsSnapshot` is saved at result creation so future course-credit edits do not change GPA history. GPA includes all PUBLISHED results (including failures), weighted by credits. Student reads own PUBLISHED results only.

**`data/grade_reviews.txt` — sole writer M5**  
`reviewId | resultId | studentId | reason | status | submittedAt | reviewedByAdminId? | reviewNote? | originalQuizMark? | originalAssignmentMark? | originalFinalExamMark? | correctedQuizMark? | correctedAssignmentMark? | correctedFinalExamMark? | resolvedAt?`  
`status`: `PENDING`, `APPROVED`, `REJECTED`, `APPLIED`. Original mark snapshot is mandatory when approval begins; corrected values must be all supplied and valid before APPLIED. Recalculation of a published result must retain the original/corrected values and reviewer in its grade review record; a separate workflow journal is optional in v0.2. One unresolved review per result at a time.

### M6 — Invoices, Payments and Refunds

**`data/invoices.txt` — sole writer M6**  
`invoiceId | enrollmentId | studentId | offeringId | currency | lineId | lineDescription | lineAmount | totalAmount | status | issuedAt | paidAt? | voidedAt?`  
`status`: `ISSUED`, `PAID`, `VOID`, `REFUNDED`. Exactly one `InvoiceLine` (`COURSE_FEE`) per paid offering/enrollment in version 1; totalAmount must equal lineAmount. Immutable once issued: `lineAmount`, `totalAmount`, `currency`, `studentId`, `offeringId` and `enrollmentId` cannot change. Unique `enrollmentId` among invoices. No invoice for 0.00 free offering.

**`data/payments.txt` — sole writer M6**  
`paymentId | invoiceId | studentId | method | idempotencyKey | externalReference? | amount | currency | status | submittedAt | verifiedAt? | verifiedByAdminId? | failureReason?`  
`method`: `DEMO`, `MANUAL`; `status`: `PENDING_VERIFICATION`, `VERIFIED`, `REJECTED`, `FAILED`. `amount` must match invoice exact amount/currency; `idempotencyKey` unique for each student payment operation (retry returns the prior outcome, not another VERIFIED payment). Nonblank manual `externalReference`; the same external reference cannot verify a different invoice. A demo payment may be VERIFIED in the creation workflow; it must be validated server-side while enrollment seat hold remains valid.

**`data/refunds.txt` — sole writer M6**  
`refundId | paymentId | studentId | amount | currency | reason | status | requestedAt | decidedAt? | decidedByAdminId? | completedAt? | decisionNote?`  
`status`: `REQUESTED`, `APPROVED`, `REJECTED`, `COMPLETED`. V1 only supports full **simulated** refund of one VERIFIED payment; prevent duplicate successful refund for same payment. A completed refund results in invoice REFUNDED and authorized M4 enrollment withdrawal if allowed; no real money moves.

### Optional leader-owned shared files (NOT required for v0.2 delivery)

The previous v0.1 proposed `data/workflow_journal.txt` with six fields `txnId | operation | primaryId | stage | payload | recordedAt` and `data/audit_log.txt` with eight fields `auditId | actorRole | actorId | action | entityType | entityId | detail | at`. **They are not part of the required v0.2 student-project deliverables.** Existing lesson files may remain for learning and must not be used as evidence of complete crash recovery. Do not make M1–M6 depend on either file. If the team later restores these as requirements, approve a separate contract revision.

## Safe illustration (not runnable seed data)

```
#SCHEMA|registration_requests|1
REQ_11111111-1111-4111-8111-111111111111|STU_22222222-2222-4222-8222-222222222222|OFF_33333333-3333-4333-8333-333333333333|1|PENDING|2026-10-18T08%3A15%3A30Z||||
```

The date contains `%3A` because **all** fields are URL-encoded, including timestamps; `URLDecoder` restores `2026-10-18T08:15:30Z`. This example is illustrative only; UUIDs are fabricated.

```
#SCHEMA|courses|1
CRS_11111111-1111-4111-8111-111111111111|CS101|Intro+to+Java|Beginner+course|3|STANDARD|0|PUBLISHED|2026-10-18T08%3A00%3A00Z|2026-10-18T08%3A00%3A00Z
```

## File ownership matrix

| Writer | Writable TXT files |
|---|---|
| M1 | `students.txt`, `admins.txt` |
| M2 | `courses.txt`, `course_offerings.txt` |
| M3 | `lecturers.txt`, `course_schedules.txt`, `prerequisites.txt` |
| M4 | `registration_requests.txt`, `enrollments.txt` |
| M5 | `results.txt`, `grade_reviews.txt` |
| M6 | `invoices.txt`, `payments.txt`, `refunds.txt` |
| Leader (optional only) | `workflow_journal.txt`, `audit_log.txt` — not required, and never touched by member modules |

**Important limit:** process-local TXT operations are not crash-atomic across files, provide no database ACID guarantees or multi-instance safety. The educational acceptance tests cover restart persistence, valid/invalid records, FIFO rebuild, last seat and payment retries—not automatic cross-file recovery.
