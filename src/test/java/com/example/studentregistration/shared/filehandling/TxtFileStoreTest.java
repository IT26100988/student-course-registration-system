
package com.example.studentregistration.shared.filehandling;

import com.example.studentregistration.shared.exception.FileStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TxtFileStoreTest {

    @Test
    void writesReadsAndPreservesData() throws Exception {

        TxtFileStore writer = new TxtFileStore("data");

        String record = "DEMO01|Hello+TXT+Storage";

        writer.writeRecords("lesson05_demo", List.of(record));

        // A different object reads the saved file.
        TxtFileStore reader = new TxtFileStore("data");

        ArrayList<String> records =
                reader.readRecords("lesson05_demo");

        assertEquals(1, records.size());
        assertEquals(record, records.get(0));

        Path path = Path.of("data", "lesson05_demo.txt");
        assertTrue(Files.exists(path));

        String fileText = Files.readString(path);
        assertTrue(fileText.startsWith("#SCHEMA|lesson05_demo|1"));

        System.out.println("TXT WRITE SUCCESS");
        System.out.println("TXT READ SUCCESS");
        System.out.println("Saved record: " + records.get(0));
    }

    @Test
    void rejectsInvalidSchemaHeader(@TempDir Path tempDir)
            throws Exception {

        Path damaged = tempDir.resolve("broken.txt");

        Files.writeString(
                damaged,
                "WRONG_HEADER\nDEMO01|Example\n",
                StandardCharsets.UTF_8);

        TxtFileStore store = new TxtFileStore(tempDir.toString());

        assertThrows(
                FileStorageException.class,
                () -> store.readRecords("broken"));
    }
}
