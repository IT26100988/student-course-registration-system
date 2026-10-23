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
                            " | Fee: " +
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
                "\n========== AVAILABILITY =========="
        );

        for (Course c :
                service.getAllCourses()) {

            System.out.println(
                    c.getCourseName()
                            + " -> "
                            + c.isAvailable()
            );
        }

        System.out.println(
                "\n========== DELETE =========="
        );

        boolean deleted =
                service.deleteCourse("C004");

        System.out.println(
                "Deleted : " + deleted
        );
    }
}