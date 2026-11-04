package com.example.studentregistration.accounts.controller;

import com.example.studentregistration.accounts.dto.RegisterStudentRequest;
import com.example.studentregistration.accounts.dto.RegisterStudentResponse;
import com.example.studentregistration.accounts.service.RegistrationService;
import com.example.studentregistration.shared.exception.FileStorageException;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    // Student Registration
    @PostMapping("/register")
    public ResponseEntity<RegisterStudentResponse> registerStudent(
            @Valid @RequestBody RegisterStudentRequest request)
            throws FileStorageException {

        RegisterStudentResponse response =
                registrationService.registerStudent(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}