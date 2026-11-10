package com.example.studentregistration.scheduling.rule;

import com.example.studentregistration.scheduling.model.Prerequisite;
import com.example.studentregistration.scheduling.model.RecordStatus;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class PrerequisiteGraphValidatorTest {

    private final PrerequisiteGraphValidator validator =
            new PrerequisiteGraphValidator();

    @Test
    public void detectsSelfReference() {
        assertTrue(
                validator.isSelfReference("COURSE_A", "COURSE_A")
        );
    }

    @Test
    public void detectsDuplicateActivePrerequisite() {
        ArrayList<Prerequisite> prerequisites = new ArrayList<>();
        prerequisites.add(new Prerequisite(
                "PRQ_1",
                "COURSE_A",
                "COURSE_B",
                RecordStatus.ACTIVE,
                null,
                null
        ));

        assertTrue(validator.hasDuplicateActivePair(
                "COURSE_A",
                "COURSE_B",
                prerequisites,
                null
        ));
    }

    @Test
    public void detectsCycle() {
        ArrayList<Prerequisite> prerequisites = new ArrayList<>();
        prerequisites.add(new Prerequisite(
                "PRQ_1",
                "COURSE_A",
                "COURSE_B",
                RecordStatus.ACTIVE,
                null,
                null
        ));

        assertTrue(validator.wouldCreateCycle(
                "COURSE_B",
                "COURSE_A",
                prerequisites,
                null
        ));
    }

    @Test
    public void allowsRuleThatDoesNotCreateCycle() {
        ArrayList<Prerequisite> prerequisites = new ArrayList<>();
        prerequisites.add(new Prerequisite(
                "PRQ_1",
                "COURSE_A",
                "COURSE_B",
                RecordStatus.ACTIVE,
                null,
                null
        ));

        assertFalse(validator.wouldCreateCycle(
                "COURSE_C",
                "COURSE_A",
                prerequisites,
                null
        ));
    }
}