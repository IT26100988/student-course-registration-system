package com.example.studentregistration.accounts.service;

import com.example.studentregistration.accounts.dto.RegisterStudentRequest;
import com.example.studentregistration.accounts.dto.RegisterStudentResponse;
import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.accounts.repository.StudentRepository;
import com.example.studentregistration.shared.exception.FileStorageException;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RegistrationService {

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    public RegistrationService(
            StudentService studentService,
            StudentRepository studentRepository) {

        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }

    public RegisterStudentResponse registerStudent(RegisterStudentRequest request)
            throws FileStorageException {

        // Check duplicate Student Number
        if (studentRepository.existsByStudentNo(request.getStudentNo())) {
            throw new IllegalArgumentException(
                    "Student number already exists");
        }

        // Check duplicate Email
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email already exists");
        }

        String studentId = "STU_" + UUID.randomUUID();

        // Store password without encryption
        String password = request.getPassword();

        String currentTime = Instant.now().toString();

        Student student = new Student(
                studentId,
                request.getFullName(),
                request.getEmail(),
                password,
                AccountStatus.ACTIVE,
                currentTime,
                currentTime,
                request.getStudentNo()
        );

        studentService.addStudent(student);

        return new RegisterStudentResponse(
                student.getId(),
                student.getStudentNo(),
                student.getFullName(),
                student.getEmail(),
                student.getStatus().name()
        );
    }
}