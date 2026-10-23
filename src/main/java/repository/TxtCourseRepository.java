package repository;

import model.Course;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TxtCourseRepository implements CourseRepository {

    private static final String FILE_PATH =
            "data/courses.txt";

    @Override
    public void addCourse(Course course) {

        try (
                BufferedWriter writer =
                        new BufferedWriter(
                                new FileWriter(FILE_PATH, true))
        ) {

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
        return new ArrayList<>();
    }
}