package com.example.studentregistration.Course.service;

import com.example.studentregistration.Course.model.Course;
import com.example.studentregistration.Course.repository.CourseRepository;

import java.util.ArrayList;
import java.util.List;

public class CourseCatalogService {

    private final CourseRepository repository;

    public CourseCatalogService(
            CourseRepository repository) {

        this.repository = repository;
    }

    public void addCourse(Course course) {
        repository.addCourse(course);
    }

    public List<Course> getAllCourses() {
        return repository.getAllCourses();
    }

    public Course findCourseById(
            String courseId) {

        return repository.findById(courseId);
    }

    public boolean deleteCourse(
            String courseId) {

        return repository.deleteCourse(courseId);
    }

    public boolean updateCourse(
            Course course) {

        return repository.updateCourse(course);
    }

    public List<Course> filterAvailableCourses() {

        List<Course> available =
                new ArrayList<>();

        for (Course course :
                repository.getAllCourses()) {

            if (course.isAvailable()) {
                available.add(course);
            }
        }

        return available;
    }
}