package com.example.studentregistration.accounts.service;

import com.example.studentregistration.accounts.dto.LoginRequest;
import com.example.studentregistration.accounts.dto.LoginResponse;
import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.accounts.model.Admin;
import com.example.studentregistration.accounts.repository.StudentRepository;
import com.example.studentregistration.accounts.repository.AdminRepository;
import com.example.studentregistration.shared.exception.FileStorageException;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LoginService {

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    public LoginService(
            StudentRepository studentRepository,
            AdminRepository adminRepository) {

        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
    }

    public LoginResponse login(LoginRequest request)
            throws FileStorageException {

        String email = request.getEmail();
        String password = request.getPassword();

        // Check Student Account
        Optional<Student> studentOptional =
                studentRepository.findByEmail(email);

        if (studentOptional.isPresent()) {

            Student student = studentOptional.get();

            if (student.getStatus() != AccountStatus.ACTIVE) {
                throw new IllegalArgumentException(
                        "Your account is inactive");
            }

            if (!student.getPasswordHash().equals(password)) {
                throw new IllegalArgumentException(
                        "Invalid email or password");
            }

            return new LoginResponse(
                    student.getId(),
                    "STUDENT",
                    student.getFullName()
            );
        }

        // Check Admin Account
        Optional<Admin> adminOptional =
                adminRepository.findByEmail(email);

        if (adminOptional.isPresent()) {

            Admin admin = adminOptional.get();

            if (admin.getStatus() != AccountStatus.ACTIVE) {
                throw new IllegalArgumentException(
                        "Your account is inactive");
            }

            if (!admin.getPasswordHash().equals(password)) {
                throw new IllegalArgumentException(
                        "Invalid email or password");
            }

            return new LoginResponse(
                    admin.getId(),
                    "ADMIN",
                    admin.getFullName()
            );
        }

        throw new IllegalArgumentException(
                "Invalid email or password");
    }
}