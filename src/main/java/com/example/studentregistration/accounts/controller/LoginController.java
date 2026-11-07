package com.example.studentregistration.accounts.controller;

import com.example.studentregistration.accounts.dto.LoginRequest;
import com.example.studentregistration.accounts.dto.LoginResponse;
import com.example.studentregistration.accounts.service.LoginService;
import com.example.studentregistration.shared.exception.FileStorageException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class LoginController {

    private final LoginService loginService;

    public LoginController(LoginService loginService) {
        this.loginService = loginService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest)
            throws FileStorageException {

        try {
            LoginResponse response = loginService.login(request);

            // Invalidate existing session if available
            HttpSession oldSession = httpRequest.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Create a new session
            HttpSession session = httpRequest.getSession(true);

            // Store logged-in user details
            session.setAttribute("userId", response.getUserId());
            session.setAttribute("role", response.getRole());
            session.setAttribute("fullName", response.getFullName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", e.getMessage()));
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpRequest) {

        HttpSession session = httpRequest.getSession(false);

        if (session == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "No active session found"));
        }

        session.invalidate();

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully")
        );
    }
}