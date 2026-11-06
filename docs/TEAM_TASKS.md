# TEAM TASKS — FROZEN v0.2

**Simplified v0.2:** Ignore mandatory journal/automatic-crash-recovery lessons. Focus on functional screens, APIs, TXT CRUD, FIFO, prerequisites, insertion sort, payment and integration. All six members work in **one repository** and must own relevant **Student + Admin** UI. The CRUD columns are business workflows; a status change is an UPDATE, not a physical DELETE. Every member provides success, failure and restart-persistence tests.

| Fixed ID | Branch | Student pages | Admin pages | Three distinct CRUD categories (minimum C/R/U) | Meaningful `ArrayList<T>` use | Meaningful OOP |
|---|---|---|---|---|---|---|
| **M1** | `feature/accounts` | `student/profile.html`, `student/login.html`, `student/register.html` | `admin/students.html` | C: register/create student; R: view/search profiles; U: edit profile/password/status | `ArrayList<Student>` loaded from `students.txt`, filtered/modified and safely rewritten | abstract `User` → `Student`, `Admin`; repository interface polymorphism |
| **M2** | `feature/courses` | `student/courses.html` | `admin/courses.html` | C: create course/offering; R: browse/search; U: fee/capacity/window/status | `ArrayList<Course>` and `ArrayList<CourseOffering>` filtering/update | `Course` → `StandardCourse`/`PremiumCourse`; Course has independent Offerings |
| **M3** | `feature/lecturers-prerequisites` | `student/timetable.html`, `student/prerequisites.html` | `admin/lecturers.html`, `admin/schedules.html`, `admin/prerequisites.html` | C: lecturer/schedule/rule; R: timetable/rule check; U: lecturer/schedule/rule | `ArrayList<Prerequisite>` graph checks, `ArrayList<CourseSchedule>` collision detection | Lecturer — Offering association; `ScheduleConflictChecker` interface |
| **M4** | `feature/registration-enrollment` | `student/registrations.html` | `admin/registration-queue.html`, `admin/enrollments.html` | C: submit request and create enrollment; R: pending/history/roster; U: approve/reject/cancel/activate | `ArrayList<RegistrationRequest>` from TXT plus real `Queue<RegistrationRequest>` FIFO | `RegistrationQueue` aggregates requests; `RegistrationRule` interface |
| **M5** | `feature/results-sorting` | `student/results.html`, `student/grade-reviews.html` | `admin/results.html`, `admin/grade-reviews.html`, `admin/registration-order.html` | C: draft grade/review; R: GPA/results/reports; U: marks/publish/review | `ArrayList<CourseResult>`, `ArrayList<RegistrationTimeView>` for insertion sort | `CourseResult` composes AssessmentMarks; abstract Assessment types |
| **M6** | `feature/payments` | `student/payments.html`, `student/refunds.html` | `admin/payments.html`, `admin/refunds.html` | C: pay/request refund; R: invoice/receipt/status; U: verify/reject/refund | `ArrayList<Payment>` for history, reference duplication/reconciliation | `Invoice` composes `InvoiceLine`; `PaymentProcessor` strategy |

## Definition of done for EACH member

1. Relevant POJO(s), `Repository` interface and `Txt...Repository`, owning `Service`, REST Controller, request/response DTOs and custom exception.
2. At least **three distinct CRUD categories** C/R/U. Deletion only where safe; cancellation/archival/voiding is explicitly a status update.
3. `ArrayList<T>`: actually read file → parse → add/search/filter/get/set/remove/iterate (as applicable) → safely persist. Explain temporary collection versus permanent TXT records.
4. Student and Admin HTML pages plus vanilla-JS `fetch` wiring into the integrated shared shell.
5. Positive, negative, role/ownership, and persistence-after-restart tests, documented with expected JSON/file effects.
6. Viva explanation: how repository interfaces differ from concrete TXT classes; the particular OOP relationship; what the file owner writes; module API.

## Dependencies and delivery checkpoints

- **L2 (leader):** first verify the existing Maven project compiles; publish ONE GitHub baseline with v0.2 contract docs, minimal shared TXT utility, agreed shared ports/DTOs, errors and dashboard shells as small steps. Do not implement every module or further automatic-recovery infrastructure.
- **M1:** accounts + session auth; provides `StudentAccountPort`; first integrated routes and role guards.
- **M2:** courses + offerings + fee snapshots; provides `OfferingCatalogPort`.
- **M3:** timetables and prerequisite graph; consumes M2, M4 read port and M5 pass port; provides `SchedulingEligibilityPort`.
- **M4:** request+queue+enrollment; consumes M1, M2, M3 and M6 invoice port; provides `EnrollmentReadPort`, `EnrollmentPaymentPort` and `RegistrationTimestampPort`.
- **M6:** issues invoices, verifies demo/manual payments, initiates refunds; consumes M4 enrollment payment port. Does NOT edit enrollments.txt.
- **M5:** results, pass checks, grade reviews and insertion sort; consumes M4 read and timestamp ports and M2 credits; provides `PassedCoursePort`.

M3, M4, M5 and M6 can work in parallel against strict leader-supplied port mocks. Compiling against a mock is not equivalent to passing a real end-to-end test.

## Merge and review policy

- The real GitHub URL is supplied only by leader at L2; no invented remote.
- Use the six fixed branches above; no one commits module development directly to `main`.
- Every PR includes owned files only, contract version, passed tests, screenshot of Student page and Admin page, sample fake TXT before/after, and cross-port dependencies.
- Suggested integration checkpoints: (1) baseline; (2) M1+M2; (3) M3; (4) M4 with prereq checks and FIFO after restart; (5) M6 activation and retry; (6) M5 grade, GPA, sort; (7) all browsers + restart.
- Leader approves contract amendments and broadcasts changed docs to all members **before** refactoring dependent code.
