package service;

import model.Course;
import repository.CourseRepository;

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
}