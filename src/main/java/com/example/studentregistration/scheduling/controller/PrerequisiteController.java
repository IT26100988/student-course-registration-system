package com.example.studentregistration.scheduling.controller;

import com.example.studentregistration.scheduling.model.Prerequisite;
import com.example.studentregistration.scheduling.model.RecordStatus;
import com.example.studentregistration.scheduling.service.PrerequisiteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class PrerequisiteController {

    private final PrerequisiteService prerequisiteService;

    public PrerequisiteController(PrerequisiteService prerequisiteService) {
        this.prerequisiteService = prerequisiteService;
    }

    @GetMapping("/courses/{courseId}/prerequisites")
    public ResponseEntity<?> getCoursePrerequisites(
            @PathVariable String courseId
    ) {
        try {
            ArrayList<Map<String, Object>> items = new ArrayList<>();

            for (Prerequisite prerequisite
                    : prerequisiteService.getAllPrerequisites()) {
                if (prerequisite.getCourseId().equals(courseId)
                        && prerequisite.getStatus() == RecordStatus.ACTIVE) {
                    items.add(Map.of(
                            "prerequisiteId",
                            prerequisite.getPrerequisiteId(),
                            "courseId",
                            prerequisite.getCourseId(),
                            "requiredCourseId",
                            prerequisite.getRequiredCourseId()
                    ));
                }
            }

            return ResponseEntity.ok(Map.of(
                    "items", items,
                    "total", items.size()
            ));
        } catch (IOException exception) {
            return ResponseEntity.internalServerError().body(
                    Map.of("message", "Could not read prerequisite data.")
            );
        }
    }
}