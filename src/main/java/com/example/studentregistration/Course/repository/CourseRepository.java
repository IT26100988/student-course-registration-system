package com.example.studentregistrationsystem.repository;

import com.example.studentregistrationsystem.model.Course;

import java.util.List;

public interface CourseRepository {

    void addCourse(Course course);

    List<Course> getAllCourses();

    Course findById(String courseId);

    boolean deleteCourse(String courseId);

    boolean updateCourse(Course updatedCourse);
}