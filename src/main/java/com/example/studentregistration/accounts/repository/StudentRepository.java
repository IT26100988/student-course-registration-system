package com.example.studentregistration.accounts.repository;

import com.example.studentregistration.accounts.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for Student persistence.
 * No implementation here — TxtStudentRepository will implement this.
 */
public interface StudentRepository {

    /** Return all students from storage. */
    List<Student> findAll();

    /** Find one student by id; Optional.empty() if not found. */
    Optional<Student> findById(String studentId);

    /** Find one student by studentNo (unique). */
    Optional<Student> findByStudentNo(String studentNo);

    /** Find one student by email (case-insensitive unique check). */
    Optional<Student> findByEmail(String email);

    /** Insert a new student. Return the saved student. */
    Student save(Student student);

    /** Update an existing student. Return the updated student. */
    Student update(Student student);
}
