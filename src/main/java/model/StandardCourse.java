package model;

public class StandardCourse extends Course {

    public StandardCourse(
            String courseId,
            String courseName,
            double fee,
            int capacity,
            int enrolledStudents) {

        super(
                courseId,
                courseName,
                fee,
                capacity,
                enrolledStudents
        );
    }
}