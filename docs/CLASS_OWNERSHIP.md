# CLASS OWNERSHIP — FROZEN v0.2

**Base:** `src/main/java/com/example/studentregistration/`  
Every name below is relative to this base. `*.java` implied for Java paths. Feature-root packages are intentional; do not create another owner's type in a different package.

## Leader — shared infrastructure (not a seventh feature module)

- `StudentRegistrationApplication.java`
- `shared/filehandling/{TxtCodec,TxtFileStore,StorageLockManager}.java`
- `shared/workflow/SharedWorkflowCoordinator.java` — existing simple lock coordinator may be reused for the M4/M6 workflow; do **not** expand it into crash recovery.
- `shared/workflow/WorkflowRecoveryService.java`, `WorkflowJournalStore.java`, `WorkflowPayloadCodec.java` — existing lesson/experimental code is **optional**, never a member deliverable or an integration prerequisite.
- `shared/exception/{FileStorageException,GlobalExceptionHandler}.java`
- `shared/config/{StorageConfig,SecurityConfig,BootstrapAdminConfig}.java` (leader scaffolds; M1 integrates user service/security)
- `shared/port/{StudentAccountPort,OfferingCatalogPort,SchedulingEligibilityPort,EnrollmentReadPort,PassedCoursePort,InvoiceIssuePort,EnrollmentPaymentPort,RegistrationTimestampPort}.java`
- `shared/dto/{StudentEligibilityView,CourseSnapshot,OfferingSnapshot,EnrollmentView,InvoiceView,RegistrationTimeView,ApiError}.java`
- `src/main/resources/static/{student/index.html,admin/index.html,css/common.css,js/api.js}`
- `data/workflow_journal.txt`, `data/audit_log.txt` — **optional learning artifacts, not required**. No member writes them.

## M1 — `accounts/`

- Models: `model/{User,Student,Admin,AccountStatus,UserRole}`.
- Repositories: `repository/{StudentRepository,AdminRepository}`; `repository/txt/{TxtStudentRepository,TxtAdminRepository}`.
- Services: `service/{StudentService,AuthenticationService}`; implements `StudentAccountPort`.
- Controllers: `controller/{AuthController,StudentController,AdminStudentController}`.
- DTO: `dto/{RegisterStudentRequest,LoginRequest,StudentProfileResponse,UpdateStudentRequest,ChangePasswordRequest,UpdateStudentStatusRequest}`.
- Exceptions: `exception/{DuplicateStudentException,StudentNotFoundException,InvalidCredentialsException}`.
- Files: `data/{students.txt,admins.txt}`.
- UI: `static/student/{login.html,register.html,profile.html}`, `static/admin/students.html`; scripts `static/js/accounts.js`.

## M2 — `courses/`

- Models: `model/{Course,StandardCourse,PremiumCourse,CourseOffering,CourseType,CourseStatus,OfferingStatus}`.
- Repositories: `repository/{CourseRepository,CourseOfferingRepository}`; `repository/txt/{TxtCourseRepository,TxtCourseOfferingRepository}`.
- Services: `service/{CourseCatalogService,CourseOfferingService}`; implements `OfferingCatalogPort`.
- Controllers: `controller/{CourseController,OfferingController,AdminCourseController}`.
- DTO: `dto/{CreateCourseRequest,UpdateCourseRequest,CreateOfferingRequest,UpdateOfferingRequest,CourseResponse,OfferingResponse}`.
- Exceptions: `exception/{CourseNotFoundException,OfferingNotOpenException}`.
- Files: `data/{courses.txt,course_offerings.txt}`.
- UI: `static/student/courses.html`, `static/admin/courses.html`, `static/js/courses.js`.

## M3 — `scheduling/`

- Models: `model/{Lecturer,CourseSchedule,Prerequisite,RecordStatus}`.
- Repositories: `repository/{LecturerRepository,CourseScheduleRepository,PrerequisiteRepository}`; `repository/txt/{TxtLecturerRepository,TxtCourseScheduleRepository,TxtPrerequisiteRepository}`.
- Services: `service/{LecturerService,TimetableService,PrerequisiteService}`; implements `SchedulingEligibilityPort`.
- Rules: `rule/{ScheduleConflictChecker,WeeklyScheduleConflictChecker,PrerequisiteGraphValidator}`.
- Controllers: `controller/{LecturerController,TimetableController,PrerequisiteController,AdminSchedulingController}`.
- DTO: `dto/{CreateLecturerRequest,UpdateLecturerRequest,CreateScheduleRequest,UpdateScheduleRequest,CreatePrerequisiteRequest,UpdatePrerequisiteRequest,EligibilityResponse}`.
- Exceptions: `exception/{ScheduleConflictException,PrerequisiteNotMetException,PrerequisiteCycleException}`.
- Files: `data/{lecturers.txt,course_schedules.txt,prerequisites.txt}`.
- UI: `static/student/{timetable.html,prerequisites.html}`, `static/admin/{lecturers.html,schedules.html,prerequisites.html}`, `static/js/scheduling.js`.

## M4 — `registration/`

- Models: `model/{RegistrationRequest,RequestStatus,Enrollment,EnrollmentStatus}`.
- Repositories: `repository/{RegistrationRequestRepository,EnrollmentRepository}`; `repository/txt/{TxtRegistrationRequestRepository,TxtEnrollmentRepository}`.
- Services: `service/{RegistrationRequestService,EnrollmentLifecycleService}`; implements `EnrollmentReadPort`, `EnrollmentPaymentPort`, `RegistrationTimestampPort`.
- Queue: `datastructure/RegistrationQueue` (wraps real `Queue<RegistrationRequest>` / `ArrayDeque`).
- Rules: `rule/{RegistrationRule,ActiveStudentRule,OpenOfferingRule,CapacityRule,DuplicateRequestRule,SchedulePrerequisiteRule}`.
- Controllers: `controller/{StudentRegistrationController,AdminRegistrationController,EnrollmentController}`.
- DTO: `dto/{SubmitRegistrationRequest,RegistrationRequestResponse,EnrollmentResponse,DecideNextRequestRequest,WithdrawEnrollmentRequest}`.
- Exceptions: `exception/{RegistrationNotAllowedException,QueueEmptyException,InsufficientSeatsException}`.
- Files: `data/{registration_requests.txt,enrollments.txt}`. **M4 is the sole writer of enrollments.txt.**
- UI: `static/student/registrations.html`, `static/admin/{registration-queue.html,enrollments.html}`, `static/js/registration.js`.

## M5 — `results/`

- Models: `model/{CourseResult,ResultStatus,AssessmentMark,Assessment,QuizAssessment,AssignmentAssessment,FinalExamAssessment,GradeReviewRequest,GradeReviewStatus}`.
- Repositories: `repository/{ResultRepository,GradeReviewRepository}`; `repository/txt/{TxtResultRepository,TxtGradeReviewRepository}`.
- Services: `service/{ResultService,GradeReviewService}`; implements `PassedCoursePort`.
- Algorithm: `algorithm/StudentInsertionSort` (manual, no library sort).
- Controllers: `controller/{StudentResultController,AdminResultController,GradeReviewController,RegistrationOrderReportController}`.
- DTO: `dto/{CreateDraftResultRequest,UpdateDraftMarksRequest,CreateGradeReviewRequest,DecideGradeReviewRequest,ResultResponse,GpaResponse,RegistrationOrderResponse}`.
- Exceptions: `exception/{ResultNotFoundException,ResultNotPublishableException,GradeReviewConflictException}`.
- Files: `data/{results.txt,grade_reviews.txt}`.
- UI: `static/student/{results.html,grade-reviews.html}`, `static/admin/{results.html,grade-reviews.html,registration-order.html}`, `static/js/results.js`.

## M6 — `payments/`

- Models: `model/{Invoice,InvoiceLine,InvoiceStatus,Payment,PaymentMethod,PaymentStatus,Refund,RefundStatus}`.
- Repositories: `repository/{InvoiceRepository,PaymentRepository,RefundRepository}`; `repository/txt/{TxtInvoiceRepository,TxtPaymentRepository,TxtRefundRepository}`.
- Services: `service/{InvoiceService,PaymentService,RefundService}`; `InvoiceService` implements `InvoiceIssuePort`.
- Payment strategies: `processor/{PaymentProcessor,DemoPaymentProcessor,ManualPaymentProcessor}`.
- Controllers: `controller/{StudentPaymentController,AdminPaymentController,RefundController}`.
- DTO: `dto/{DemoPaymentRequest,ManualPaymentRequest,VerifyPaymentRequest,RefundRequestDto,RefundDecisionRequest,InvoiceResponse,PaymentResponse,ReceiptResponse}`.
- Exceptions: `exception/{InvoiceNotPayableException,DuplicatePaymentReferenceException,PaymentVerificationException}`.
- Files: `data/{invoices.txt,payments.txt,refunds.txt}`. **M6 never edits enrollments.txt.**
- UI: `static/student/{payments.html,refunds.html}`, `static/admin/{payments.html,refunds.html}`, `static/js/payments.js`.

## Relation cheatsheet

- `abstract User` `<|--` `Student`, `Admin` (IS-A inheritance).
- `abstract Course` `<|--` `StandardCourse`, `PremiumCourse` (premium must have >0 mentoring sessions; standard has 0).
- `Course` `1 -- 0..*` `CourseOffering` (association; independently persisted IDs).
- `Lecturer` `1 -- 0..*` `CourseSchedule`; `CourseOffering` `1 -- 0..*` `CourseSchedule` (associations).
- `RegistrationQueue` `o-- 0..* RegistrationRequest` (aggregation; queue reconstructs from persisted independent requests).
- `CourseResult` `*-- 3 AssessmentMark` (composition; assessment marks owned by result).
- `Invoice` `*-- 1 InvoiceLine` (composition; one course fee line for each enrollment under this v1 scope).
- `PaymentProcessor` `<|..` `DemoPaymentProcessor`, `ManualPaymentProcessor` (interface realization).
- Each `...Repository` interface `<|..` `Txt...Repository` concrete implementation.
