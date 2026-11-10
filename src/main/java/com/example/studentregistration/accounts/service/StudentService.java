package com.example.studentregistration.accounts.service;

import com.example.studentregistration.accounts.exception.DuplicateStudentException;
import com.example.studentregistration.accounts.exception.StudentNotFoundException;
import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.accounts.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for Student accounts (M1).
 * Controller -> Service -> Repository -> TXT file.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /** CREATE — register a new student. */
    public Student register(String studentNo, String fullName, String email,
                            String passwordHash) {
        validateRequired(studentNo, "studentNo");
        validateRequired(fullName, "fullName");
        validateRequired(email, "email");
        validateRequired(passwordHash, "passwordHash");

        if (studentRepository.findByStudentNo(studentNo).isPresent()) {
            throw new DuplicateStudentException("studentNo already exists: " + studentNo);
        }
        if (studentRepository.findByEmail(email).isPresent()) {
            throw new DuplicateStudentException("email already exists: " + email);
        }

        Instant now = Instant.now();
        Student student = new Student(
                "STU_" + UUID.randomUUID(),
                studentNo.trim(),
                fullName.trim(),
                email.trim().toLowerCase(),
                passwordHash,
                AccountStatus.ACTIVE,
                now,
                now
        );
        return studentRepository.save(student);
    }

    /** READ — list all. */
    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    /** READ — search by name/email/studentNo. */
    public List<Student> search(String query) {
        if (query == null || query.isBlank()) return findAll();
        String q = query.toLowerCase().trim();
        return studentRepository.findAll().stream()
                .filter(s -> s.getFullName().toLowerCase().contains(q)
                        || s.getEmail().toLowerCase().contains(q)
                        || s.getStudentNo().toLowerCase().contains(q))
                .toList();
    }

    /** READ — one by id. */
    public Student findById(String studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentId));
    }

    public Optional<Student> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        return studentRepository.findByEmail(email.trim().toLowerCase());
    }

    /** UPDATE — profile (fullName, email). */
    public Student updateProfile(String studentId, String fullName, String email) {
        Student existing = findById(studentId);

        if (fullName != null && !fullName.isBlank()) {
            existing.setFullName(fullName.trim());
        }
        if (email != null && !email.isBlank()) {
            String normalized = email.trim().toLowerCase();
            Optional<Student> byEmail = studentRepository.findByEmail(normalized);
            if (byEmail.isPresent() && !byEmail.get().getId().equals(studentId)) {
                throw new DuplicateStudentException("email already exists: " + normalized);
            }
            existing.setEmail(normalized);
        }
        existing.setUpdatedAt(Instant.now());
        return studentRepository.update(existing);
    }

    /** UPDATE — status (ACTIVE/INACTIVE). */
    public Student updateStatus(String studentId, AccountStatus status) {
        Student existing = findById(studentId);
        existing.setStatus(status);
        existing.setUpdatedAt(Instant.now());
        return studentRepository.update(existing);
    }

    /** UPDATE — password hash. */
    public void updatePasswordHash(String studentId, String newPasswordHash) {
        Student existing = findById(studentId);
        existing.setPasswordHash(newPasswordHash);
        existing.setUpdatedAt(Instant.now());
        studentRepository.update(existing);
    }

    private void validateRequired(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }
}