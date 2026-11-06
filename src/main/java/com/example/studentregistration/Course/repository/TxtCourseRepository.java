package com.example.studentregistration.Course.repository;

import com.example.studentregistration.Course.model.Course;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TxtCourseRepository implements CourseRepository {

    private static final String FILE_PATH = "data/courses.txt";

    @Override
    public void addCourse(Course course) {
        ensureDirectoryExists();

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(FILE_PATH, true))) {

            writer.write(
                    course.getCourseId() + "," +
                            course.getCourseName() + "," +
                            course.getFee() + "," +
                            course.getCapacity() + "," +
                            course.getEnrolledStudents()
            );

            writer.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Course> getAllCourses() {

        List<Course> courses = new ArrayList<>();
        File file = new File(FILE_PATH);

        // File එක තවම හැදිලා නැත්නම් Exception එකක් නොදී empty list එකක් return කරයි
        if (!file.exists()) {
            return courses;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",");

                if (data.length == 5) {

                    courses.add(
                            new Course(
                                    data[0],
                                    data[1],
                                    Double.parseDouble(data[2]),
                                    Integer.parseInt(data[3]),
                                    Integer.parseInt(data[4])
                            )
                    );
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return courses;
    }

    @Override
    public Course findById(String courseId) {

        for (Course course : getAllCourses()) {

            if (course.getCourseId()
                    .equalsIgnoreCase(courseId)) {

                return course;
            }
        }

        return null;
    }

    @Override
    public boolean deleteCourse(String courseId) {

        List<Course> courses = getAllCourses();

        boolean removed =
                courses.removeIf(course ->
                        course.getCourseId()
                                .equalsIgnoreCase(courseId));

        if (removed) {
            saveAllCourses(courses);
        }

        return removed;
    }

    @Override
    public boolean updateCourse(
            Course updatedCourse) {

        List<Course> courses =
                getAllCourses();

        boolean found = false;

        for (int i = 0; i < courses.size(); i++) {

            if (courses.get(i)
                    .getCourseId()
                    .equalsIgnoreCase(
                            updatedCourse.getCourseId())) {

                courses.set(i, updatedCourse);

                found = true;
                break;
            }
        }

        if (found) {
            saveAllCourses(courses);
        }

        return found;
    }

    private void saveAllCourses(
            List<Course> courses) {

        ensureDirectoryExists();

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter(FILE_PATH))
        ) {

            for (Course course : courses) {

                writer.write(
                        course.getCourseId() + "," +
                                course.getCourseName() + "," +
                                course.getFee() + "," +
                                course.getCapacity() + "," +
                                course.getEnrolledStudents()
                );

                writer.newLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // data folder එක නැත්නම් auto create කරන method එක
    private void ensureDirectoryExists() {
        File file = new File(FILE_PATH);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }
}