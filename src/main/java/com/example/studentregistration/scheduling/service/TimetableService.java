package com.example.studentregistration.scheduling.service;

import com.example.studentregistration.scheduling.exception.ScheduleConflictException;
import com.example.studentregistration.scheduling.model.CourseSchedule;
import com.example.studentregistration.scheduling.model.Lecturer;
import com.example.studentregistration.scheduling.model.RecordStatus;
import com.example.studentregistration.scheduling.repository.CourseScheduleRepository;
import com.example.studentregistration.scheduling.repository.LecturerRepository;
import com.example.studentregistration.scheduling.rule.ScheduleConflictChecker;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class TimetableService {
    private final CourseScheduleRepository scheduleRepository;
    private final LecturerRepository lecturerRepository;
    private final ScheduleConflictChecker conflictChecker;

    public TimetableService(
            CourseScheduleRepository scheduleRepository,
            LecturerRepository lecturerRepository,
            ScheduleConflictChecker conflictChecker) {
        this.scheduleRepository = scheduleRepository;
        this.lecturerRepository = lecturerRepository;
        this.conflictChecker = conflictChecker;
    }

    public ArrayList<CourseSchedule> getAllSchedules() {
        try {
            return scheduleRepository.findAll();
        } catch (IOException error) {
            throw storageError(error);
        }
    }

    public ArrayList<CourseSchedule> getSchedulesForOffering(
            String offeringId) {
        ArrayList<CourseSchedule> result = new ArrayList<>();

        for (CourseSchedule schedule : getAllSchedules()) {
            if (schedule.getOfferingId().equals(offeringId)
                    && schedule.getStatus() == RecordStatus.ACTIVE) {
                result.add(schedule);
            }
        }

        return result;
    }

    public CourseSchedule getScheduleById(String scheduleId) {
        try {
            CourseSchedule schedule = scheduleRepository.findById(scheduleId);

            if (schedule == null) {
                throw new IllegalArgumentException("Schedule was not found.");
            }

            return schedule;
        } catch (IOException error) {
            throw storageError(error);
        }
    }

    public CourseSchedule createSchedule(CourseSchedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule is required.");
        }

        schedule.setScheduleId("SCH_" + UUID.randomUUID());
        schedule.setStatus(RecordStatus.ACTIVE);

        Instant now = Instant.now();
        schedule.setCreatedAt(now);
        schedule.setUpdatedAt(now);

        validateSchedule(schedule);

        try {
            requireActiveLecturer(schedule.getLecturerId());

            ArrayList<CourseSchedule> existingSchedules =
                    scheduleRepository.findAll();

            if (conflictChecker.hasConflict(schedule, existingSchedules)) {
                throw new ScheduleConflictException(
                        "The lecturer or room has a time conflict.");
            }

            scheduleRepository.save(schedule);
            return schedule;
        } catch (IOException error) {
            throw storageError(error);
        }
    }

    public CourseSchedule updateSchedule(
            String scheduleId, CourseSchedule changes) {
        if (changes == null) {
            throw new IllegalArgumentException("Schedule changes are required.");
        }

        try {
            CourseSchedule existing =
                    scheduleRepository.findById(scheduleId);

            if (existing == null) {
                throw new IllegalArgumentException("Schedule was not found.");
            }

            if (changes.getOfferingId() != null) {
                existing.setOfferingId(changes.getOfferingId());
            }
            if (changes.getLecturerId() != null) {
                existing.setLecturerId(changes.getLecturerId());
            }
            if (changes.getDayOfWeek() != null) {
                existing.setDayOfWeek(changes.getDayOfWeek());
            }
            if (changes.getStartLocal() != null) {
                existing.setStartLocal(changes.getStartLocal());
            }
            if (changes.getEndLocal() != null) {
                existing.setEndLocal(changes.getEndLocal());
            }
            if (changes.getTimeZone() != null) {
                existing.setTimeZone(changes.getTimeZone());
            }
            if (changes.getRoom() != null) {
                existing.setRoom(changes.getRoom());
            }
            if (changes.getStatus() != null) {
                existing.setStatus(changes.getStatus());
            }

            validateSchedule(existing);

            if (existing.getStatus() == RecordStatus.ACTIVE) {
                requireActiveLecturer(existing.getLecturerId());
            }

            ArrayList<CourseSchedule> existingSchedules =
                    scheduleRepository.findAll();

            if (conflictChecker.hasConflict(existing, existingSchedules)) {
                throw new ScheduleConflictException(
                        "The lecturer or room has a time conflict.");
            }

            existing.setUpdatedAt(Instant.now());
            scheduleRepository.update(existing);
            return existing;
        } catch (IOException error) {
            throw storageError(error);
        }
    }

    private void requireActiveLecturer(String lecturerId)
            throws IOException {
        Lecturer lecturer = lecturerRepository.findById(lecturerId);

        if (lecturer == null || lecturer.getStatus() != RecordStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "An active lecturer is required.");
        }
    }

    private void validateSchedule(CourseSchedule schedule) {
        if (isBlank(schedule.getOfferingId())
                || isBlank(schedule.getLecturerId())
                || schedule.getDayOfWeek() == null
                || schedule.getStartLocal() == null
                || schedule.getEndLocal() == null
                || !schedule.getStartLocal().isBefore(schedule.getEndLocal())
                || !"Asia/Colombo".equals(schedule.getTimeZone())
                || isBlank(schedule.getRoom())
                || schedule.getStatus() == null) {
            throw new IllegalArgumentException(
                    "One or more schedule fields are invalid.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private IllegalStateException storageError(IOException error) {
        return new IllegalStateException(
                "Could not access schedule or lecturer TXT data.", error);
    }
}