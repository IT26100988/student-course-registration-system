package com.example.studentregistration.scheduling.rule;

import com.example.studentregistration.scheduling.model.Prerequisite;
import com.example.studentregistration.scheduling.model.RecordStatus;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class PrerequisiteGraphValidator {

    public boolean isSelfReference(String courseId, String requiredCourseId) {
        return courseId.equals(requiredCourseId);
    }

    public boolean hasDuplicateActivePair(
            String courseId,
            String requiredCourseId,
            ArrayList<Prerequisite> prerequisites,
            String excludedPrerequisiteId) {

        for (Prerequisite prerequisite : prerequisites) {
            boolean isExcluded = prerequisite.getPrerequisiteId()
                    .equals(excludedPrerequisiteId);

            boolean isSameActivePair =
                    prerequisite.getStatus() == RecordStatus.ACTIVE
                            && prerequisite.getCourseId().equals(courseId)
                            && prerequisite.getRequiredCourseId()
                            .equals(requiredCourseId);

            if (!isExcluded && isSameActivePair) {
                return true;
            }
        }

        return false;
    }

    public boolean wouldCreateCycle(
            String courseId,
            String requiredCourseId,
            ArrayList<Prerequisite> prerequisites,
            String excludedPrerequisiteId) {

        if (isSelfReference(courseId, requiredCourseId)) {
            return true;
        }

        ArrayList<String> visitedCourseIds = new ArrayList<>();

        return hasPath(
                requiredCourseId,
                courseId,
                prerequisites,
                visitedCourseIds,
                excludedPrerequisiteId
        );
    }

    private boolean hasPath(
            String currentCourseId,
            String targetCourseId,
            ArrayList<Prerequisite> prerequisites,
            ArrayList<String> visitedCourseIds,
            String excludedPrerequisiteId) {

        if (currentCourseId.equals(targetCourseId)) {
            return true;
        }

        if (visitedCourseIds.contains(currentCourseId)) {
            return false;
        }

        visitedCourseIds.add(currentCourseId);

        for (Prerequisite prerequisite : prerequisites) {
            boolean isExcluded = prerequisite.getPrerequisiteId()
                    .equals(excludedPrerequisiteId);

            boolean startsFromCurrentCourse =
                    prerequisite.getCourseId().equals(currentCourseId);

            if (!isExcluded
                    && prerequisite.getStatus() == RecordStatus.ACTIVE
                    && startsFromCurrentCourse) {
                boolean pathFound = hasPath(
                        prerequisite.getRequiredCourseId(),
                        targetCourseId,
                        prerequisites,
                        visitedCourseIds,
                        excludedPrerequisiteId
                );

                if (pathFound) {
                    return true;
                }
            }
        }

        return false;
    }
}