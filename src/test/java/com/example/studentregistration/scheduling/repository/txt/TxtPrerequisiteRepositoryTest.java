package com.example.studentregistration.scheduling.repository.txt;

import com.example.studentregistration.scheduling.model.Prerequisite;
import com.example.studentregistration.scheduling.model.RecordStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TxtPrerequisiteRepositoryTest {

    private Path temporaryDirectory;

    @BeforeEach
    void createTemporaryDirectory() throws IOException {
        temporaryDirectory =
                Files.createTempDirectory("prerequisite-test-");
    }

    @AfterEach
    void removeTemporaryFiles() throws IOException {
        Files.deleteIfExists(
                temporaryDirectory.resolve("prerequisites.txt")
        );
        Files.deleteIfExists(temporaryDirectory);
    }

    @Test
    void savedPrerequisiteCanBeLoadedAfterRepositoryRestart()
            throws IOException {
        TxtPrerequisiteRepository firstRepository =
                new TxtPrerequisiteRepository(
                        temporaryDirectory.toString()
                );

        Prerequisite prerequisite = new Prerequisite(
                "PRQ_1",
                "COURSE_A",
                "COURSE_B",
                RecordStatus.ACTIVE,
                null,
                null
        );

        firstRepository.save(prerequisite);

        TxtPrerequisiteRepository restartedRepository =
                new TxtPrerequisiteRepository(
                        temporaryDirectory.toString()
                );

        ArrayList<Prerequisite> loadedPrerequisites =
                restartedRepository.findAll();

        assertEquals(loadedPrerequisites.size(), 1);

        Prerequisite loadedPrerequisite = loadedPrerequisites.get(0);

        assertEquals(loadedPrerequisite.getPrerequisiteId(), "PRQ_1");
        assertEquals(loadedPrerequisite.getCourseId(), "COURSE_A");
        assertEquals(
                loadedPrerequisite.getRequiredCourseId(),
                "COURSE_B"
        );
        assertEquals(
                loadedPrerequisite.getStatus(),
                RecordStatus.ACTIVE
        );
        assertNotNull(loadedPrerequisite.getCreatedAt());
    }
}