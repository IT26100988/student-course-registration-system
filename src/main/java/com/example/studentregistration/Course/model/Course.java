package com.example.studentregistrationsystem.model;

public class Course {

    private String courseId;
    private String courseName;
    private double fee;
    private int capacity;
    private int enrolledStudents;

    public Course() {

    }

    public Course(String courseId,
                  String courseName,
                  double fee,
                  int capacity,
                  int enrolledStudents) {

        this.courseId = courseId;
        this.courseName = courseName;
        this.fee = fee;
        this.capacity = capacity;
        this.enrolledStudents = enrolledStudents;
    }

    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public double getFee() {
        return fee;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnrolledStudents() {
        return enrolledStudents;
    }

    public boolean isAvailable() {
        return enrolledStudents < capacity;
    }
}