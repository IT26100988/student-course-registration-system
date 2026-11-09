package com.example.studentregistration.scheduling.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class CreateScheduleRequest {

    private String offeringId;
    private String lecturerId;
    private DayOfWeek dayOfWeek;
    private LocalTime startLocal;
    private LocalTime endLocal;
    private String timeZone;
    private String room;

    public CreateScheduleRequest() {
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
}