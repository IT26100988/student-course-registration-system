package com.example.studentregistration.scheduling.controller;

import com.example.studentregistration.scheduling.model.CourseSchedule;
import com.example.studentregistration.scheduling.service.TimetableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class TimetableController {

    private final TimetableService timetableService;

    public TimetableController(TimetableService timetableService) {
        this.timetableService = timetableService;
    }

    @GetMapping("/offerings/{offeringId}/schedule")
    public ResponseEntity<Map<String, Object>> getOfferingSchedule(
            @PathVariable String offeringId
    ) {
        ArrayList<Map<String, Object>> items = new ArrayList<>();

        for (CourseSchedule schedule
                : timetableService.getSchedulesForOffering(offeringId)) {
            items.add(Map.of(
                    "scheduleId", schedule.getScheduleId(),
                    "lecturerId", schedule.getLecturerId(),
                    "dayOfWeek", schedule.getDayOfWeek(),
                    "startLocal", schedule.getStartLocal(),
                    "endLocal", schedule.getEndLocal(),
                    "timeZone", schedule.getTimeZone(),
                    "room", schedule.getRoom()
            ));
        }

        return ResponseEntity.ok(Map.of(
                "items", items,
                "total", items.size()
        ));
    }
}