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
                        "C005",
                        "Spring Boot",
                        40000,
                        25,
                        8
                );

        service.addCourse(course);

        System.out.println("Course Saved Successfully!");

        System.out.println("\nAll Courses");

        for (Course c : service.getAllCourses()) {

            System.out.println(
                    c.getCourseId() + " | " +
                            c.getCourseName() + " | " +
                            c.getFee()
            );
        }

        System.out.println("\nSearch Result");

        Course foundCourse =
                service.findCourseById("C002");

        if (foundCourse != null) {

            System.out.println(
                    foundCourse.getCourseId() + " | " +
                            foundCourse.getCourseName() + " | " +
                            foundCourse.getFee()
            );

        } else {

            System.out.println("Course Not Found!");
        }
    }
}