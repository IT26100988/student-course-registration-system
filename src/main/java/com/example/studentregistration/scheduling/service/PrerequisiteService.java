package com.example.studentregistration.scheduling.service;

import com.example.studentregistration.scheduling.dto.EligibilityResponse;
import com.example.studentregistration.scheduling.exception.PrerequisiteCycleException;
import com.example.studentregistration.scheduling.model.Prerequisite;
import com.example.studentregistration.scheduling.model.RecordStatus;
import com.example.studentregistration.scheduling.repository.PrerequisiteRepository;

import java.io.IOException;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
@Service
public class PrerequisiteService {

    private final PrerequisiteRepository prerequisiteRepository;

    public PrerequisiteService(PrerequisiteRepository prerequisiteRepository) {
        this.prerequisiteRepository = prerequisiteRepository;
    }

    public ArrayList<Prerequisite> getAllPrerequisites() throws IOException {
        return prerequisiteRepository.findAll();
    }

    public Prerequisite getPrerequisiteById(String prerequisiteId)
            throws IOException {
        return prerequisiteRepository.findById(prerequisiteId);
    }

    public void createPrerequisite(Prerequisite prerequisite) throws IOException {
        validateCourseIds(prerequisite);

        ArrayList<Prerequisite> existing =
                prerequisiteRepository.findAll();

        checkDuplicate(existing, prerequisite, null);
        checkForCycle(existing, prerequisite, null);

        prerequisiteRepository.save(prerequisite);
    }

    public void updatePrerequisite(Prerequisite prerequisite) throws IOException {
        validateCourseIds(prerequisite);

        ArrayList<Prerequisite> existing =
                prerequisiteRepository.findAll();

        checkDuplicate(
                existing,
                prerequisite,
                prerequisite.getPrerequisiteId()
        );

        checkForCycle(
                existing,
                prerequisite,
                prerequisite.getPrerequisiteId()
        );

        prerequisiteRepository.update(prerequisite);
    }

    public EligibilityResponse checkEligibility(
            String courseId,
            ArrayList<String> completedCourseIds
    ) throws IOException {
        if (courseId == null || courseId.isBlank()) {
            throw new IllegalArgumentException("Course ID is required.");
        }

        if (completedCourseIds == null) {
            completedCourseIds = new ArrayList<>();
        }

        ArrayList<String> missingCourseIds = new ArrayList<>();
        ArrayList<Prerequisite> prerequisites =
                prerequisiteRepository.findAll();

        for (Prerequisite prerequisite : prerequisites) {
            if (isActive(prerequisite)
                    && prerequisite.getCourseId().equals(courseId)
                    && !completedCourseIds.contains(
                    prerequisite.getRequiredCourseId()
            )) {
                missingCourseIds.add(
                        prerequisite.getRequiredCourseId()
                );
            }
        }

        boolean eligible = missingCourseIds.isEmpty();

        String message = eligible
                ? "Student has completed all prerequisite courses."
                : "Student has missing prerequisite courses.";

        return new EligibilityResponse(
                eligible,
                missingCourseIds,
                message
        );
    }

    private void validateCourseIds(Prerequisite prerequisite) {
        if (prerequisite == null
                || prerequisite.getCourseId() == null
                || prerequisite.getRequiredCourseId() == null
                || prerequisite.getCourseId().isBlank()
                || prerequisite.getRequiredCourseId().isBlank()) {
            throw new IllegalArgumentException(
                    "Course ID and required course ID are required."
            );
        }

        if (prerequisite.getCourseId()
                .equals(prerequisite.getRequiredCourseId())) {
            throw new PrerequisiteCycleException(
                    "A course cannot be its own prerequisite."
            );
        }
    }

    private void checkDuplicate(
            ArrayList<Prerequisite> existing,
            Prerequisite candidate,
            String ignoredPrerequisiteId
    ) {
        if (!isActive(candidate)) {
            return;
        }

        for (Prerequisite item : existing) {
            if (item.getPrerequisiteId().equals(ignoredPrerequisiteId)) {
                continue;
            }

            if (isActive(item)
                    && item.getCourseId().equals(candidate.getCourseId())
                    && item.getRequiredCourseId()
                    .equals(candidate.getRequiredCourseId())) {
                throw new IllegalArgumentException(
                        "This prerequisite rule already exists."
                );
            }
        }
    }

    private void checkForCycle(
            ArrayList<Prerequisite> existing,
            Prerequisite candidate,
            String ignoredPrerequisiteId
    ) {
        if (!isActive(candidate)) {
            return;
        }

        ArrayList<Prerequisite> rulesToCheck = new ArrayList<>();

        for (Prerequisite item : existing) {
            if (isActive(item)
                    && !item.getPrerequisiteId().equals(ignoredPrerequisiteId)) {
                rulesToCheck.add(item);
            }
        }

        if (isActive(candidate)) {
            rulesToCheck.add(candidate);
        }

        if (pathExists(
                rulesToCheck,
                candidate.getRequiredCourseId(),
                candidate.getCourseId()
        )) {
            throw new PrerequisiteCycleException(
                    "This prerequisite rule creates a cycle."
            );
        }
    }

    private boolean pathExists(
            ArrayList<Prerequisite> rules,
            String currentCourseId,
            String targetCourseId
    ) {
        return pathExists(
                rules,
                currentCourseId,
                targetCourseId,
                new ArrayList<>()
        );
    }

    private boolean pathExists(
            ArrayList<Prerequisite> rules,
            String currentCourseId,
            String targetCourseId,
            ArrayList<String> visitedCourseIds
    ) {
        if (currentCourseId.equals(targetCourseId)) {
            return true;
        }

        if (visitedCourseIds.contains(currentCourseId)) {
            return false;
        }
        visitedCourseIds.add(currentCourseId);

        for (Prerequisite rule : rules) {
            if (rule.getCourseId().equals(currentCourseId)
                    && pathExists(
                    rules,
                    rule.getRequiredCourseId(),
                    targetCourseId,
                    visitedCourseIds
            )) {
                return true;
            }
        }

        return false;
    }

    private boolean isActive(Prerequisite prerequisite) {
        return prerequisite.getStatus() == RecordStatus.ACTIVE;
    }
}
