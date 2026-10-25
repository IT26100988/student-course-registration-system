package com.example.studentregistration.scheduling.model;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

public class CourseSchedule {
    private String scheduleId;
    private String offeringId;
    private String lecturerId;
    private DayOfWeek dayOfWeek;
    private LocalTime startLocal;
    private LocalTime endLocal;
    private String timeZone;
    private String room;
    private RecordStatus status;
    private Instant createdAt;
    private Instant updatedAt;

    public CourseSchedule() {
    }

    public CourseSchedule(String scheduleId, String offeringId, String lecturerId,
                          DayOfWeek dayOfWeek, LocalTime startLocal, LocalTime endLocal,
                          String timeZone, String room, RecordStatus status,
                          Instant createdAt, Instant updatedAt) {
        this.scheduleId = scheduleId;
        this.offeringId = offeringId;
        this.lecturerId = lecturerId;
        this.dayOfWeek = dayOfWeek;
        this.startLocal = startLocal;
        this.endLocal = endLocal;
        this.timeZone = timeZone;
        this.room = room;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getOfferingId() {
        return offeringId;
    }

    public void setOfferingId(String offeringId) {
        this.offeringId = offeringId;
    }

    public String getLecturerId() {
        return lecturerId;
    }

    public void setLecturerId(String lecturerId) {
        this.lecturerId = lecturerId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartLocal() {
        return startLocal;
    }

    public void setStartLocal(LocalTime startLocal) {
        this.startLocal = startLocal;
    }

    public LocalTime getEndLocal() {
        return endLocal;
    }

    public void setEndLocal(LocalTime endLocal) {
        this.endLocal = endLocal;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public RecordStatus getStatus() {
        return status;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}