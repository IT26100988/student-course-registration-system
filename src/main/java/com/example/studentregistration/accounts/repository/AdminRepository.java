package com.example.studentregistration.accounts.repository;

import com.example.studentregistration.accounts.model.Admin;
import com.example.studentregistration.shared.exception.FileStorageException;

import java.util.List;
import java.util.Optional;

public interface AdminRepository {

    // Create
    void save(Admin admin) throws FileStorageException;

    // Read
    Optional<Admin> findById(String id)
            throws FileStorageException;

    Optional<Admin> findByEmail(String email)
            throws FileStorageException;

    List<Admin> findAll()
            throws FileStorageException;

    // Update
    boolean update(String id, Admin updatedAdmin)
            throws FileStorageException;

    // Delete
    boolean deleteById(String id)
            throws FileStorageException;
}