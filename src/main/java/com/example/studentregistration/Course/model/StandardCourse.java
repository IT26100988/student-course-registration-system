package com.example.studentregistration.Course.model;

public class StandardCourse extends Course {

    public StandardCourse() {

    }

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