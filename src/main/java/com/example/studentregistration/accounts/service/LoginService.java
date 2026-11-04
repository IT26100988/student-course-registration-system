package com.example.studentregistration.accounts.service;

import com.example.studentregistration.accounts.dto.LoginRequest;
import com.example.studentregistration.accounts.dto.LoginResponse;
import com.example.studentregistration.accounts.repository.StudentRepository;

import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final StudentRepository studentRepository;

    public LoginService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public LoginResponse login(LoginRequest request) {

        // TODO: Add password verification
        // TODO: Add session management

        throw new UnsupportedOperationException(
                "Login functionality is not completed yet"
        );
    }
}