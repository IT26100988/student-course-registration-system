package com.example.studentregistration.accounts.repository;

import com.example.studentregistration.accounts.model.Admin;

import java.util.List;
import java.util.Optional;

/**
 * Repository contract for Admin persistence.
 * TxtAdminRepository will implement this in a later step.
 */
public interface AdminRepository {

    List<Admin> findAll();

    Optional<Admin> findById(String adminId);

    Optional<Admin> findByEmail(String email);

    Admin save(Admin admin);

    Admin update(Admin admin);

    /** true if at least one ACTIVE admin exists (for bootstrap guard). */
    boolean existsAnyActiveAdmin();
}
