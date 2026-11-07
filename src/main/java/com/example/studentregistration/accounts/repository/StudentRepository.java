package com.example.studentregistration.accounts.repository;

import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.shared.exception.FileStorageException;

import java.util.List;
import java.util.Optional;

public interface StudentRepository {

    // Create
    void save(Student student) throws FileStorageException;

    // Read
    Optional<Student> findById(String id)
            throws FileStorageException;

    Optional<Student> findByEmail(String email)
            throws FileStorageException;

    List<Student> findAll()
            throws FileStorageException;

    // Update
    boolean update(String id, Student updatedStudent)
            throws FileStorageException;

    // Delete
    boolean deleteById(String id)
            throws FileStorageException;

    // Duplicate Checking
    boolean existsByStudentNo(String studentNo)
            throws FileStorageException;

    boolean existsByEmail(String email)
            throws FileStorageException;
}