package model;

public class PremiumCourse extends Course {

    public PremiumCourse(
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