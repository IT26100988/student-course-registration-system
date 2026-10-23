package controller;

import model.Course;
import repository.TxtCourseRepository;
import service.CourseCatalogService;

public class CourseController {

    public static void main(String[] args) {

        CourseCatalogService service =
                new CourseCatalogService(
                        new TxtCourseRepository()
                );

        Course course =
                new Course(
                        "C004",
                        "Java Advanced",
                        35000,
                        30,
                        10
                );

        service.addCourse(course);

        System.out.println(
                "Course Saved Successfully!"
        );
    }
}