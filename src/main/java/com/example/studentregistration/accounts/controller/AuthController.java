package com.example.studentregistration.accounts.controller;

import com.example.studentregistration.accounts.dto.LoginRequest;
import com.example.studentregistration.accounts.dto.RegisterStudentRequest;
import com.example.studentregistration.accounts.dto.StudentProfileResponse;
import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.accounts.model.User;
import com.example.studentregistration.accounts.service.AuthenticationService;
import com.example.studentregistration.accounts.service.SecurityContextService;
import com.example.studentregistration.accounts.service.StudentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final StudentService studentService;
    private final AuthenticationService authenticationService;
    private final SecurityContextService securityContextService;

    public AuthController(StudentService studentService,
                          AuthenticationService authenticationService,
                          SecurityContextService securityContextService) {
        this.studentService = studentService;
        this.authenticationService = authenticationService;
        this.securityContextService = securityContextService;
    }

    @PostMapping("/register")
    public ResponseEntity<StudentProfileResponse> register(@RequestBody RegisterStudentRequest request) {
        String hashed = authenticationService.hashPassword(request.password());
        Student created = studentService.register(
                request.studentNo(), request.fullName(), request.email(), hashed
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toProfile(created));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request,
                                                     HttpServletRequest httpRequest) {
        User user = authenticationService.login(request.email(), request.password());
        // 🔥 මේක තමයි වැදගත්ම කොටස. Session එක හදනවා.
        securityContextService.login(user, httpRequest);

        Map<String, Object> body = Map.of(
                "userId", user.getId(),
                "role", user.getRole().name(),
                "fullName", user.getFullName()
        );
        return ResponseEntity.ok(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        securityContextService.logout(httpRequest);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null
                || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Not authenticated"));
        }

        String email = auth.getName();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("UNKNOWN");

        var student = studentService.findByEmail(email);
        if (student.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "userId", student.get().getId(),
                    "role", "STUDENT",
                    "fullName", student.get().getFullName()
            ));
        }
        return ResponseEntity.ok(Map.of(
                "userId", "ADM_" + email,
                "role", role,
                "fullName", "Admin"
        ));
    }

    private StudentProfileResponse toProfile(Student s) {
        return new StudentProfileResponse(
                s.getId(), s.getStudentNo(), s.getFullName(), s.getEmail(), s.getStatus().name()
        );
    }
}