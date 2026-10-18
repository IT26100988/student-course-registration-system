
package com.example.studentregistration.shared.filehandling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class Lesson07NoteStoreTest {

    @Test
    void testsAllCrudOperations(@TempDir Path directory)
            throws Exception {

        Lesson07NoteStore notes =
                new Lesson07NoteStore(directory);

        assertTrue(notes.listIds().isEmpty());

        // CREATE
        notes.create("N01", "First note");
        notes.create("N02", "Java | Spring");

        assertEquals(List.of("N01", "N02"), notes.listIds());
        System.out.println("CREATE -> " + notes.listIds());

        // READ using a new object
        Lesson07NoteStore reloaded =
                new Lesson07NoteStore(directory);

        assertEquals("First note", reloaded.read("N01"));
        System.out.println("READ -> " + reloaded.read("N01"));

        // UPDATE
        reloaded.update("N01", "Updated + saved");

        Lesson07NoteStore afterUpdate =
                new Lesson07NoteStore(directory);

        assertEquals("Updated + saved", afterUpdate.read("N01"));
        System.out.println("UPDATE -> " + afterUpdate.read("N01"));

        // DELETE
        afterUpdate.delete("N01");

        assertEquals(List.of("N02"), afterUpdate.listIds());
        System.out.println("DELETE -> " + afterUpdate.listIds());

        // Verify actual TXT file content
        Path savedFile = directory.resolve("lesson07_notes.txt");

        List<String> diskLines = Files.readAllLines(
                savedFile, StandardCharsets.UTF_8
        );

        assertEquals(List.of(
                "#SCHEMA|lesson07_notes|1",
                "N02|Java+%7C+Spring"
        ), diskLines);

        assertThrows(
                NoSuchElementException.class,
                () -> afterUpdate.read("N01")
        );

        System.out.println("TXT CRUD TEST PASSED");
    }

    @Test
    void rejectsDuplicateAndMissingNotes(@TempDir Path directory)
            throws Exception {

        Lesson07NoteStore notes =
                new Lesson07NoteStore(directory);

        notes.create("N01", "Original");

        assertThrows(
                IllegalArgumentException.class,
                () -> notes.create("N01", "Duplicate")
        );

        assertThrows(
                NoSuchElementException.class,
                () -> notes.update("N99", "Missing")
        );

        assertThrows(
                NoSuchElementException.class,
                () -> notes.delete("N99")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> notes.create("", "Invalid ID")
        );

        assertEquals("Original", notes.read("N01"));
        assertEquals(1, notes.listIds().size());

        System.out.println("CRUD ERROR TESTS PASSED");
    }
}
