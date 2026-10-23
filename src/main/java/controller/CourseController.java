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

        System.out.println(
                "========== ALL COURSES =========="
        );

        for (Course c :
                service.getAllCourses()) {

            System.out.println(
                    c.getCourseId() +
                            " | " +
                            c.getCourseName() +
                            " | Fee : " +
                            c.getFee()
            );
        }

        System.out.println(
                "\n========== SEARCH =========="
        );

        Course course =
                service.findCourseById("C002");

        if (course != null) {

            System.out.println(
                    course.getCourseName()
            );
        }

        System.out.println(
                "\n========== AVAILABLE COURSES =========="
        );

        for (Course c :
                service.filterAvailableCourses()) {

            System.out.println(
                    c.getCourseName()
            );
        }

        System.out.println(
                "\n========== UPDATE =========="
        );

        Course updatedCourse =
                new Course(
                        "C002",
                        "Advanced Web Development",
                        45000,
                        50,
                        20
                );

        boolean updated =
                service.updateCourse(
                        updatedCourse);

        System.out.println(
                "Updated : " + updated
        );

        System.out.println(
                "\n========== FEES =========="
        );

        for (Course c :
                service.getAllCourses()) {

            System.out.println(
                    c.getCourseName() +
                            " -> Rs." +
                            c.getFee()
            );
        }
    }
}