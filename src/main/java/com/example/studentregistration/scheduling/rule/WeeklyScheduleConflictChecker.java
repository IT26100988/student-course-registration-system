package com.example.studentregistration.scheduling.rule;

import com.example.studentregistration.scheduling.model.CourseSchedule;
import com.example.studentregistration.scheduling.model.RecordStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;

@Component
public class WeeklyScheduleConflictChecker
        implements ScheduleConflictChecker {

    @Override
    public boolean hasConflict(
            CourseSchedule candidate,
            ArrayList<CourseSchedule> existingSchedules) {

        if (candidate.getStatus() != RecordStatus.ACTIVE) {
            return false;
        }

        for (CourseSchedule existing : existingSchedules) {
            if (existing.getStatus() != RecordStatus.ACTIVE) {
                continue;
            }

            if (Objects.equals(
                    candidate.getScheduleId(),
                    existing.getScheduleId())) {
                continue;
            }

            if (candidate.getDayOfWeek() != existing.getDayOfWeek()) {
                continue;
            }

            if (!timesOverlap(candidate, existing)) {
                continue;
            }

            boolean sameLecturer = candidate.getLecturerId()
                    .equals(existing.getLecturerId());

            boolean sameRoom = candidate.getRoom()
                    .equalsIgnoreCase(existing.getRoom());

            if (sameLecturer || sameRoom) {
                return true;
            }
        }

        return false;
    }

    private boolean timesOverlap(
            CourseSchedule first,
            CourseSchedule second) {

        return first.getStartLocal().isBefore(second.getEndLocal())
                && second.getStartLocal().isBefore(first.getEndLocal());
    }
}