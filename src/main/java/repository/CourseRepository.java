package repository;

import model.Course;
import java.util.List;

public interface CourseRepository {

    void addCourse(Course course);

    List<Course> getAllCourses();
}