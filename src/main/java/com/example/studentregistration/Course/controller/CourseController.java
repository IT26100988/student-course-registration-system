package com.example.studentregistration.Course.controller;

import com.example.studentregistration.Course.model.Course;
import com.example.studentregistration.Course.repository.TxtCourseRepository;
import com.example.studentregistration.Course.service.CourseCatalogService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@CrossOrigin
public class CourseController {

    private final CourseCatalogService service =
            new CourseCatalogService(
                    new TxtCourseRepository()
            );

    @GetMapping
    public List<Course> getAllCourses() {
        return service.getAllCourses();
    }

    @GetMapping("/{id}")
    public Course getCourseById(
            @PathVariable String id) {

        return service.findCourseById(id);
    }

    @PostMapping
    public String addCourse(
            @RequestBody Course course) {

        service.addCourse(course);

        return "Course Added Successfully";
    }

    @PutMapping
    public String updateCourse(
            @RequestBody Course course) {

        boolean updated =
                service.updateCourse(course);

        return updated
                ? "Course Updated Successfully"
                : "Course Not Found";
    }

    @DeleteMapping("/{id}")
    public String deleteCourse(
            @PathVariable String id) {

        boolean deleted =
                service.deleteCourse(id);

        return deleted
                ? "Course Deleted Successfully"
                : "Course Not Found";
    }
}