package com.example.studentregistration.scheduling.rule;

import com.example.studentregistration.scheduling.model.CourseSchedule;

import java.util.ArrayList;

public interface ScheduleConflictChecker {
    boolean hasConflict(
            CourseSchedule candidate,
            ArrayList<CourseSchedule> existingSchedules);
}