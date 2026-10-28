package com.example.studentregistration.repository;

import com.example.studentregistration.model.Admin;

import java.util.List;
import java.util.Optional;

public interface AdminRepository {

    void save(Admin admin);

    Optional<Admin> findById(String id);

    List<Admin> findAll();

    boolean update(String id, Admin updatedAdmin);

    boolean deleteById(String id);
}