package com.example.studentregistration.repository;

import com.example.studentregistration.model.Student;
import java.util.List;
import java.util.Optional;

public interface StudentRepository {

    void save(Student student);

    Optional<Student> findById(String id);

    List<Student> findAll();

    boolean update(String id, Student updatedStudent);

    boolean deleteById(String id);
}