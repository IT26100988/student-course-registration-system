package com.example.studentregistration.accounts.service;

import com.example.studentregistration.accounts.dto.StudentProfileResponse;
import com.example.studentregistration.accounts.dto.UpdateStudentRequest;
import com.example.studentregistration.accounts.dto.UpdateStudentStatusRequest;

import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Student;

import com.example.studentregistration.accounts.repository.StudentRepository;
import com.example.studentregistration.shared.exception.FileStorageException;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    // Add a new student
    public void addStudent(Student student)
            throws FileStorageException {

        studentRepository.save(student);
    }

    // Find student by ID
    public Optional<Student> getStudentById(String id)
            throws FileStorageException {

        return studentRepository.findById(id);
    }

    // Get student profile by ID
    public Optional<StudentProfileResponse> getStudentProfile(String id)
            throws FileStorageException {

        return studentRepository.findById(id)
                .map(this::convertToResponse);
    }

    // Get all student profiles and search students
    public List<StudentProfileResponse> getAllStudentProfiles(String query)
            throws FileStorageException {

        return studentRepository.findAll()
                .stream()
                .filter(student -> matchesQuery(student, query))
                .map(this::convertToResponse)
                .toList();
    }

    // Update student profile
    public Optional<StudentProfileResponse> updateStudentProfile(
            String id,
            UpdateStudentRequest request)
            throws FileStorageException {

        Optional<Student> optionalStudent =
                studentRepository.findById(id);

        if (optionalStudent.isEmpty()) {
            return Optional.empty();
        }

        if (request.getFullName() == null
                && request.getEmail() == null) {

            throw new IllegalArgumentException(
                    "At least one field is required");
        }

        Student student = optionalStudent.get();

        // Update full name
        if (request.getFullName() != null) {

            if (request.getFullName().isBlank()) {
                throw new IllegalArgumentException(
                        "Full name cannot be empty");
            }

            student.setFullName(request.getFullName().trim());
        }

        // Update email
        if (request.getEmail() != null) {

            if (request.getEmail().isBlank()) {
                throw new IllegalArgumentException(
                        "Email cannot be empty");
            }

            String newEmail = request.getEmail().trim();

            boolean emailExists = studentRepository.findAll()
                    .stream()
                    .anyMatch(s ->
                            !s.getId().equals(id)
                                    && s.getEmail().equalsIgnoreCase(newEmail));

            if (emailExists) {
                throw new IllegalArgumentException(
                        "Email already exists");
            }

            student.setEmail(newEmail);
        }

        boolean updated =
                studentRepository.update(id, student);

        if (!updated) {
            return Optional.empty();
        }

        return Optional.of(convertToResponse(student));
    }

    // Update student status (ACTIVE / INACTIVE)
    public Optional<StudentProfileResponse> updateStudentStatus(
            String id,
            UpdateStudentStatusRequest request)
            throws FileStorageException {

        if (request.getStatus() == null) {
            throw new IllegalArgumentException(
                    "Status is required");
        }

        AccountStatus status;

        try {
            status = AccountStatus.valueOf(
                    request.getStatus().trim().toUpperCase());

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "Status must be ACTIVE or INACTIVE");
        }

        Optional<Student> optionalStudent =
                studentRepository.findById(id);

        if (optionalStudent.isEmpty()) {
            return Optional.empty();
        }

        Student student = optionalStudent.get();

        student.setStatus(status);

        boolean updated =
                studentRepository.update(id, student);

        if (!updated) {
            return Optional.empty();
        }

        return Optional.of(convertToResponse(student));
    }

    // Change student password
    public boolean changeStudentPassword(
            String id,
            String currentPassword,
            String newPassword)
            throws FileStorageException {

        Optional<Student> optionalStudent =
                studentRepository.findById(id);

        if (optionalStudent.isEmpty()) {
            return false;
        }

        Student student = optionalStudent.get();

        // Check current password
        if (student.getPasswordHash() == null
                || !student.getPasswordHash().equals(currentPassword)) {

            throw new IllegalArgumentException(
                    "Current password is incorrect");
        }

        // Validate new password
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException(
                    "New password must contain at least 8 characters");
        }

        // Update password
        student.setPasswordHash(newPassword);

        return studentRepository.update(id, student);
    }

    // Search students
    private boolean matchesQuery(Student student, String query) {

        if (query == null || query.isBlank()) {
            return true;
        }

        String search = query.trim().toLowerCase();

        return student.getStudentNo().toLowerCase().contains(search)
                || student.getFullName().toLowerCase().contains(search)
                || student.getEmail().toLowerCase().contains(search);
    }

    // Convert Student object into StudentProfileResponse
    private StudentProfileResponse convertToResponse(Student student) {

        return new StudentProfileResponse(
                student.getId(),
                student.getStudentNo(),
                student.getFullName(),
                student.getEmail(),
                student.getStatus().name()
        );
    }
}