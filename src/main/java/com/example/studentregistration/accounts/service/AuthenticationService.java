package com.example.studentregistration.accounts.service;

import com.example.studentregistration.accounts.exception.InvalidCredentialsException;
import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Admin;
import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.accounts.model.User;
import com.example.studentregistration.accounts.model.UserRole;
import com.example.studentregistration.accounts.repository.AdminRepository;
import com.example.studentregistration.accounts.repository.StudentRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(StudentRepository studentRepository,
                                 AdminRepository adminRepository,
                                 PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(String email, String rawPassword) {
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw new InvalidCredentialsException("Email and password are required");
        }
        String normalized = email.trim().toLowerCase();

        // Try student first
        Optional<Student> student = studentRepository.findByEmail(normalized);
        if (student.isPresent()) {
            Student s = student.get();
            if (s.getStatus() != AccountStatus.ACTIVE) {
                throw new InvalidCredentialsException("Account is inactive");
            }
            // 🔥 Plain text check (Testing සඳහා)
            if (!rawPassword.equals(s.getPasswordHash())) {
                throw new InvalidCredentialsException("Wrong email or password");
            }
            return s;
        }

        // Try admin
        Optional<Admin> admin = adminRepository.findByEmail(normalized);
        if (admin.isPresent()) {
            Admin a = admin.get();
            if (a.getStatus() != AccountStatus.ACTIVE) {
                throw new InvalidCredentialsException("Account is inactive");
            }
            // 🔥 Plain text check (Testing සඳහා)
            if (!rawPassword.equals(a.getPasswordHash())) {
                throw new InvalidCredentialsException("Wrong email or password");
            }
            return a;
        }

        throw new InvalidCredentialsException("Wrong email or password");
    }

    /** 🔥 දැන් අපි encrypt කරන්නේ නෑ (Testing සඳහා) */
    public String hashPassword(String rawPassword) {
        return rawPassword;
    }

    public boolean needsBootstrapAdmin() {
        return !adminRepository.existsAnyActiveAdmin();
    }

    public UserRole roleOf(User user) {
        return user.getRole();
    }
}