package com.example.studentregistration.scheduling.repository;

import com.example.studentregistration.scheduling.model.CourseSchedule;

import java.io.IOException;
import java.util.ArrayList;

public interface CourseScheduleRepository {
    ArrayList<CourseSchedule> findAll() throws IOException;

    CourseSchedule findById(String scheduleId) throws IOException;

    void save(CourseSchedule schedule) throws IOException;

    void update(CourseSchedule schedule) throws IOException;
}