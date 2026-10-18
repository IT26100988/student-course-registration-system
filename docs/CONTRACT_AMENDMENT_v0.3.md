# TEAM CONTRACT AMENDMENT — v0.3 (BEGINNER-FRIENDLY OOP PROJECT)

**Approved by Team Leader on 2026-10-18.** This document AMENDS, not replaces, the approved v0.2 contract. Share this single file with EVERY team member and commit it under `docs/` in the ONE shared repository.

## Priority / conflict rule
1. **This v0.3 amendment** controls scope and implementation complexity.
2. Existing **v0.2 docs** remain the reference for member ownership, named business models, branch names, REST endpoint paths, request/response JSON fields, TXT file names, on-disk schemas, statuses and IDs, except where this amendment expressly simplifies them.
3. **Master Prompt v5.0** controls teaching steps; outdated Master Prompt v4.0 and earlier chats are superseded on architecture requirements.
4. Any newly discovered business contradiction must be resolved with the leader before a member invents fields/files/routes. Avoid redesigning working code needlessly.

## Main goal: complete a functional student OOP project, NOT advanced infrastructure
One Java 21 Spring Boot Maven project with HTML/CSS/vanilla JavaScript, Java REST endpoints, plain TXT file persistence using BufferedReader/BufferedWriter (or equivalent buffered Java Files APIs), six teammates, ONE GitHub repository, Student and Admin screens.

### All six members must demonstrate
- **Class / object / encapsulation**, appropriate Java inheritance, abstraction or polymorphism, and **a meaningful relationship** (inheritance, association, aggregation or composition). Team-wide examples: abstract User -> Student/Admin, StandardCourse/PremiumCourse, Lecturer associated with CourseOffering, RegistrationQueue aggregates RegistrationRequest, CourseResult composes AssessmentMarks, Invoice composes InvoiceLine.
- **At least one meaningful Java interface** in each module, especially a feature `Repository` interface implemented by `Txt...Repository`. Additional simple domain interfaces (RegistrationRule, PaymentProcessor etc.) only if they naturally help demonstrate OOP.
- `ArrayList<T>` for actual load -> find/add/update -> save operations on the module's owned TXT records; show **three distinct CRUD categories** (Create, Read, Update). Delete only if safe, otherwise use status/cancel.
- File handling, input validation, try/catch and a simple custom exception as appropriate. Avoid silently ignoring malformed files. Use the EXISTING `TxtFileStore`, `TxtCodec`, and basic storage synchronization helper if already present and working.
- A model, repository interface, TXT implementation, service, REST controller, Student page, Admin page, vanilla JavaScript `fetch()`, and straightforward tests. JUnit and browser/Postman examples; demonstrate TXT survives application restart.
- M3 course prerequisite checks; M4 genuine FIFO Queue with persistent order; M5 handwritten Insertion Sort by registration time; M6 DEMO/manual payment, invoices and simple refunds; both Student/Admin workflows. Basic password hashing and role checks remain important.

### Simplifications / NOT mandatory
- **Do NOT require separate shared port interfaces** such as StudentAccountPort, OfferingCatalogPort, SchedulingEligibilityPort, EnrollmentReadPort, InvoiceIssuePort, PassedCoursePort, EnrollmentPaymentPort, or RegistrationTimestampPort. Existing Step 02/03 port code may remain if it compiles; it is NOT a blocker for teammates.
- **Do NOT require snapshot-only shared DTO records** (StudentEligibilityView, CourseSnapshot, OfferingSnapshot, etc.). Use ordinary course/account/enrollment models or small return types when helpful. HTTP request/response fields should still follow API_CONTRACT.md.
- No mandatory WorkflowJournalStore, WorkflowRecoveryService, WorkflowPayloadCodec, write-ahead logging, distributed locking, automatic rollback, transaction replay, contract-mock-only integration phases, or per-port contract test suites.
- No need to build all shared integrations BEFORE member coding. Use normal Spring constructor dependency injection between module **services**, not direct cross-module TXT file/repository access. Avoid circular service dependencies; if a dependency creates a cycle, leader coordinates a small read-only service method or simple helper. Do not invent multiple competing DTOs.
- Do not add new frameworks, payment gateway/bank integrations, SQL/JPA, cloud services, microservices, advanced security frameworks, or complex operational infrastructure just for this assignment.

### Keep it simple, but functional
- Member owns writes to their TXT files ONLY. Other modules obtain facts through the owning member's service methods; **never edit another member's TXT file**.
- Use straightforward validation and basic critical-section protection where required to avoid duplicate requests, overselling seats or double payment; explain limitations of TXT storage rather than building ACID-style infrastructure.
- Keep v0.2’s endpoint and TXT field order stable so modules integrate. When implementation is difficult, complete an end-to-end working basic flow before optional enhancements; do not quietly omit course prerequisites, queue, sort, payment, or member Student/Admin pages.
- Leader integrates module Pull Requests one at a time, tests Student registration -> admin approval -> demo payment -> enrollment -> results, and asks members to resolve conflicts.

## Work order (replaces unnecessary advanced-port lessons)
1. **Leader** verifies existing project and GitHub baseline, puts THIS amendment in docs/ and shares Master Prompt v5 and member starters; no further mandatory shared port/snapshot lessons.
2. **Each member** implements their module step-by-step in their feature branch (model -> repository interface -> TXT class -> service -> controller -> Student/Admin UI -> tests).
3. **Leader** merges compatible working modules, tests the real browser flow and handles only necessary integration changes.
4. Java files created in previous lessons are NOT to be deleted automatically; keep compiling code, remove unused educational components only after verifying no references/tests rely on them.

**No changes to original six member assignments or core OOP / Queue / Insertion Sort / TXT / Payment requirements.**
