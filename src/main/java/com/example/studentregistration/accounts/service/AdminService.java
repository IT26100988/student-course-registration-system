
package com.example.studentregistration.accounts.service;

import com.example.studentregistration.accounts.model.Admin;
import com.example.studentregistration.accounts.repository.AdminRepository;
import com.example.studentregistration.shared.exception.FileStorageException;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    // Add a new admin
    public void addAdmin(Admin admin) throws FileStorageException {
        adminRepository.save(admin);
    }

    // Find admin by ID
    public Optional<Admin> getAdminById(String id)
            throws FileStorageException {
        return adminRepository.findById(id);
    }

    // Get all admins
    public List<Admin> getAllAdmins() throws FileStorageException {
        return adminRepository.findAll();
    }

    // Update admin details
    public boolean updateAdmin(String id, Admin updatedAdmin)
            throws FileStorageException {
        return adminRepository.update(id, updatedAdmin);
    }

    // Delete admin
    public boolean deleteAdmin(String id) throws FileStorageException {
        return adminRepository.deleteById(id);
    }
}