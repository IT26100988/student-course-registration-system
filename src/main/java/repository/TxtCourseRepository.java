package repository;

import model.Course;
import java.util.ArrayList;
import java.util.List;

public class TxtCourseRepository implements CourseRepository {

    @Override
    public void addCourse(Course course) {

    }

    @Override
    public List<Course> getAllCourses() {

        return new ArrayList<>();
    }
}