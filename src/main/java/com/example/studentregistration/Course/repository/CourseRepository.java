package com.example.studentregistration.Course.repository;

import com.example.studentregistration.Course.model.Course;

import java.util.List;

public interface CourseRepository {

    void addCourse(Course course);

    List<Course> getAllCourses();

    Course findById(String courseId);

    boolean deleteCourse(String courseId);

    boolean updateCourse(Course updatedCourse);
}