package com.example.studentregistration.accounts.repository;

import com.example.studentregistration.accounts.model.Admin;
import com.example.studentregistration.shared.exception.FileStorageException;

import java.util.List;
import java.util.Optional;

public interface AdminRepository {

    void save(Admin admin) throws FileStorageException;

    Optional<Admin> findById(String id) throws FileStorageException;

    List<Admin> findAll() throws FileStorageException;

    boolean update(String id, Admin updatedAdmin) throws FileStorageException;

    boolean deleteById(String id) throws FileStorageException;
}