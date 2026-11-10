package com.example.studentregistration.scheduling.controller;

import com.example.studentregistration.scheduling.dto.CreateLecturerRequest;
import com.example.studentregistration.scheduling.dto.CreatePrerequisiteRequest;
import com.example.studentregistration.scheduling.dto.CreateScheduleRequest;
import com.example.studentregistration.scheduling.dto.UpdateLecturerRequest;
import com.example.studentregistration.scheduling.dto.UpdatePrerequisiteRequest;
import com.example.studentregistration.scheduling.dto.UpdateScheduleRequest;
import com.example.studentregistration.scheduling.exception.PrerequisiteCycleException;
import com.example.studentregistration.scheduling.exception.ScheduleConflictException;
import com.example.studentregistration.scheduling.model.CourseSchedule;
import com.example.studentregistration.scheduling.model.Lecturer;
import com.example.studentregistration.scheduling.model.Prerequisite;
import com.example.studentregistration.scheduling.model.RecordStatus;
import com.example.studentregistration.scheduling.service.LecturerService;
import com.example.studentregistration.scheduling.service.PrerequisiteService;
import com.example.studentregistration.scheduling.service.TimetableService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AdminSchedulingController {

    private final LecturerService lecturerService;
    private final TimetableService timetableService;
    private final PrerequisiteService prerequisiteService;

    public AdminSchedulingController(
            LecturerService lecturerService,
            TimetableService timetableService,
            PrerequisiteService prerequisiteService
    ) {
        this.lecturerService = lecturerService;
        this.timetableService = timetableService;
        this.prerequisiteService = prerequisiteService;
    }

    @GetMapping("/admin/lecturers")
    public ResponseEntity<Map<String, Object>> getAllLecturers() {
        ArrayList<Lecturer> lecturers =
                lecturerService.getAllLecturers();

        return ResponseEntity.ok(Map.of(
                "items", lecturers,
                "total", lecturers.size()
        ));
    }

    @PostMapping("/admin/lecturers")
    public ResponseEntity<?> createLecturer(
            @RequestBody CreateLecturerRequest request
    ) {
        try {
            Lecturer lecturer = lecturerService.createLecturer(
                    request.getFullName(),
                    request.getEmail(),
                    request.getDepartment()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(lecturer);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage())
            );
        }
    }

    @PatchMapping("/admin/lecturers/{lecturerId}")
    public ResponseEntity<?> updateLecturer(
            @PathVariable String lecturerId,
            @RequestBody UpdateLecturerRequest request
    ) {
        try {
            Lecturer lecturer = lecturerService.updateLecturer(
                    lecturerId,
                    request.getFullName(),
                    request.getEmail(),
                    request.getDepartment(),
                    request.getStatus()
            );

            return ResponseEntity.ok(lecturer);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage())
            );
        }
    }

    @GetMapping("/admin/schedules")
    public ResponseEntity<Map<String, Object>> getAdminSchedules(
            @RequestParam(required = false) String offeringId
    ) {
        ArrayList<CourseSchedule> schedules =
                timetableService.getAllSchedules();

        ArrayList<CourseSchedule> matchingSchedules =
                new ArrayList<>();

        for (CourseSchedule schedule : schedules) {
            if (offeringId == null
                    || offeringId.isBlank()
                    || schedule.getOfferingId().equals(offeringId)) {
                matchingSchedules.add(schedule);
            }
        }

        return ResponseEntity.ok(Map.of(
                "items", matchingSchedules,
                "total", matchingSchedules.size()
        ));
    }

    @PostMapping("/admin/schedules")
    public ResponseEntity<?> createSchedule(
            @RequestBody CreateScheduleRequest request
    ) {
        try {
            CourseSchedule schedule = new CourseSchedule();
            schedule.setOfferingId(request.getOfferingId());
            schedule.setLecturerId(request.getLecturerId());
            schedule.setDayOfWeek(request.getDayOfWeek());
            schedule.setStartLocal(request.getStartLocal());
            schedule.setEndLocal(request.getEndLocal());
            schedule.setTimeZone(request.getTimeZone());
            schedule.setRoom(request.getRoom());

            CourseSchedule savedSchedule =
                    timetableService.createSchedule(schedule);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(savedSchedule);
        } catch (ScheduleConflictException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", exception.getMessage())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage())
            );
        }
    }

    @PatchMapping("/admin/schedules/{scheduleId}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable String scheduleId,
            @RequestBody UpdateScheduleRequest request
    ) {
        try {
            CourseSchedule changes = new CourseSchedule();
            changes.setLecturerId(request.getLecturerId());
            changes.setDayOfWeek(request.getDayOfWeek());
            changes.setStartLocal(request.getStartLocal());
            changes.setEndLocal(request.getEndLocal());
            changes.setTimeZone(request.getTimeZone());
            changes.setRoom(request.getRoom());
            changes.setStatus(request.getStatus());

            CourseSchedule updatedSchedule =
                    timetableService.updateSchedule(scheduleId, changes);

            return ResponseEntity.ok(updatedSchedule);
        } catch (ScheduleConflictException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", exception.getMessage())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage())
            );
        }
    }

    @GetMapping("/admin/prerequisites")
    public ResponseEntity<?> getAdminPrerequisites(
            @RequestParam(required = false) String courseId
    ) {
        try {
            ArrayList<Prerequisite> prerequisites =
                    prerequisiteService.getAllPrerequisites();

            ArrayList<Prerequisite> matchingPrerequisites =
                    new ArrayList<>();

            for (Prerequisite prerequisite : prerequisites) {
                if (courseId == null
                        || courseId.isBlank()
                        || prerequisite.getCourseId().equals(courseId)) {
                    matchingPrerequisites.add(prerequisite);
                }
            }

            return ResponseEntity.ok(Map.of(
                    "items", matchingPrerequisites,
                    "total", matchingPrerequisites.size()
            ));
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Could not read prerequisite data.")
            );
        }
    }

    @PostMapping("/admin/prerequisites")
    public ResponseEntity<?> createPrerequisite(
            @RequestBody CreatePrerequisiteRequest request
    ) {
        Instant now = Instant.now();

        Prerequisite prerequisite = new Prerequisite(
                "PRE_" + UUID.randomUUID(),
                request.getCourseId(),
                request.getRequiredCourseId(),
                RecordStatus.ACTIVE,
                now,
                now
        );

        try {
            prerequisiteService.createPrerequisite(prerequisite);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(prerequisite);
        } catch (PrerequisiteCycleException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", exception.getMessage())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage())
            );
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Could not save prerequisite data.")
            );
        }
    }

    @PatchMapping("/admin/prerequisites/{prerequisiteId}")
    public ResponseEntity<?> updatePrerequisite(
            @PathVariable String prerequisiteId,
            @RequestBody UpdatePrerequisiteRequest request
    ) {
        try {
            Prerequisite existing =
                    prerequisiteService.getPrerequisiteById(prerequisiteId);

            if (existing == null) {
                return ResponseEntity.notFound().build();
            }

            String requiredCourseId =
                    request.getRequiredCourseId() == null
                            ? existing.getRequiredCourseId()
                            : request.getRequiredCourseId();

            RecordStatus status =
                    request.getStatus() == null
                            ? existing.getStatus()
                            : request.getStatus();

            Prerequisite changes = new Prerequisite(
                    existing.getPrerequisiteId(),
                    existing.getCourseId(),
                    requiredCourseId,
                    status,
                    existing.getCreatedAt(),
                    Instant.now()
            );

            prerequisiteService.updatePrerequisite(changes);

            return ResponseEntity.ok(changes);
        } catch (PrerequisiteCycleException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("message", exception.getMessage())
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of("message", exception.getMessage())
            );
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Could not update prerequisite data.")
            );
        }
    }
}