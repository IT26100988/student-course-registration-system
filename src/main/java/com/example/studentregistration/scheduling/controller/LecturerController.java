package com.example.studentregistration.scheduling.controller;

import com.example.studentregistration.scheduling.model.Lecturer;
import com.example.studentregistration.scheduling.service.LecturerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class LecturerController {

    private final LecturerService lecturerService;

    public LecturerController(LecturerService lecturerService) {
        this.lecturerService = lecturerService;
    }

    @GetMapping("/lecturers/{lecturerId}")
    public ResponseEntity<?> getLecturerById(
            @PathVariable String lecturerId
    ) {
        try {
            Lecturer lecturer =
                    lecturerService.getLecturerById(lecturerId);

            Map<String, Object> response = Map.of(
                    "lecturerId", lecturer.getLecturerId(),
                    "fullName", lecturer.getFullName(),
                    "department", lecturer.getDepartment(),
                    "status", lecturer.getStatus()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}