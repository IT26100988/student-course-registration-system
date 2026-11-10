package com.example.studentregistration.scheduling.service;

import com.example.studentregistration.scheduling.model.Lecturer;
import com.example.studentregistration.scheduling.model.RecordStatus;
import com.example.studentregistration.scheduling.repository.LecturerRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class LecturerService {
    private final LecturerRepository lecturerRepository;

    public LecturerService(LecturerRepository lecturerRepository) {
        this.lecturerRepository = lecturerRepository;
    }

    public ArrayList<Lecturer> getAllLecturers() {
        try {
            return lecturerRepository.findAll();
        } catch (IOException error) {
            throw new IllegalStateException(
                    "Could not read lecturer data.", error);
        }
    }

    public Lecturer getLecturerById(String lecturerId) {
        try {
            Lecturer lecturer = lecturerRepository.findById(lecturerId);

            if (lecturer == null) {
                throw new IllegalArgumentException("Lecturer was not found.");
            }

            return lecturer;
        } catch (IOException error) {
            throw new IllegalStateException(
                    "Could not read lecturer data.", error);
        }
    }

    public Lecturer createLecturer(
            String fullName, String email, String department) {
        requireText(fullName, "Full name");
        requireText(email, "Email");
        requireText(department, "Department");

        Instant now = Instant.now();

        Lecturer lecturer = new Lecturer(
                "LEC_" + UUID.randomUUID(),
                fullName.trim(),
                email.trim(),
                department.trim(),
                RecordStatus.ACTIVE,
                now,
                now
        );

        try {
            lecturerRepository.save(lecturer);
            return lecturer;
        } catch (IOException error) {
            throw new IllegalStateException(
                    "Could not save lecturer data.", error);
        }
    }

    public Lecturer updateLecturer(
            String lecturerId,
            String fullName,
            String email,
            String department,
            RecordStatus status) {
        try {
            Lecturer lecturer = lecturerRepository.findById(lecturerId);

            if (lecturer == null) {
                throw new IllegalArgumentException("Lecturer was not found.");
            }

            if (fullName != null) {
                requireText(fullName, "Full name");
                lecturer.setFullName(fullName.trim());
            }

            if (email != null) {
                requireText(email, "Email");
                lecturer.setEmail(email.trim());
            }

            if (department != null) {
                requireText(department, "Department");
                lecturer.setDepartment(department.trim());
            }

            if (status != null) {
                lecturer.setStatus(status);
            }

            lecturer.setUpdatedAt(Instant.now());
            lecturerRepository.update(lecturer);
            return lecturer;
        } catch (IOException error) {
            throw new IllegalStateException(
                    "Could not update lecturer data.", error);
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank.");
        }
    }
}