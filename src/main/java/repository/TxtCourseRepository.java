package repository;

import model.Course;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TxtCourseRepository implements CourseRepository {

    private static final String FILE_PATH = "data/courses.txt";

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

        List<Course> courses = new ArrayList<>();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new FileReader(FILE_PATH))
        ) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",");

                if (data.length == 5) {

                    Course course =
                            new Course(
                                    data[0],
                                    data[1],
                                    Double.parseDouble(data[2]),
                                    Integer.parseInt(data[3]),
                                    Integer.parseInt(data[4])
                            );

                    courses.add(course);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return courses;
    }

    @Override
    public Course findById(String courseId) {

        List<Course> courses = getAllCourses();

        for (Course course : courses) {

            if (course.getCourseId().equalsIgnoreCase(courseId)) {
                return course;
            }
        }

        return null;
    }
}