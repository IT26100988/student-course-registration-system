package repository;

import model.Course;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TxtCourseRepository implements CourseRepository {

    private static final String FILE_PATH = "data/courses.txt";

    @Override
    public void addCourse(Course course) {

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

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(FILE_PATH))) {

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

        boolean found = false;

        courses.removeIf(course -> {

            if (course.getCourseId()
                    .equalsIgnoreCase(courseId)) {

                return true;
            }
            return false;
        });

        try (BufferedWriter writer =
                     new BufferedWriter(
                             new FileWriter(FILE_PATH))) {

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

            found = true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return found;
    }

    @Override
    public boolean updateCourse(Course updatedCourse) {

        List<Course> courses = getAllCourses();

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

            try (BufferedWriter writer =
                         new BufferedWriter(
                                 new FileWriter(FILE_PATH))) {

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

        return found;
    }
}