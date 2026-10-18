
package com.example.studentregistration.shared.filehandling;

import com.example.studentregistration.shared.exception.FileStorageException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TxtCodecTest {

    @Test
    void encodesAndDecodesSpecialCharacters()
            throws FileStorageException {

        List<String> original = List.of(
                "Nimal Perera",
                "a|b",
                "a+b",
                "50%",
                "line1\nline2",
                "සිංහල"
        );

        String encoded = TxtCodec.encodeRecord(original);

        assertTrue(encoded.contains("Nimal+Perera"));
        assertTrue(encoded.contains("a%7Cb"));
        assertTrue(encoded.contains("a%2Bb"));
        assertTrue(encoded.contains("50%25"));
        assertTrue(encoded.contains("line1%0Aline2"));

        assertFalse(encoded.contains("\n"));

        ArrayList<String> decoded =
                TxtCodec.decodeRecord(encoded, 6);

        assertEquals(original, decoded);

        System.out.println("ENCODE AND DECODE SUCCESS");
    }

    @Test
    void handlesEmptyOptionalFields()
            throws FileStorageException {

        List<String> original = Arrays.asList(
                "STU001", null, ""
        );

        String encoded = TxtCodec.encodeRecord(original);

        assertEquals("STU001||", encoded);

        ArrayList<String> decoded =
                TxtCodec.decodeRecord(encoded, 3);

        assertEquals("STU001", decoded.get(0));
        assertNull(decoded.get(1));
        assertNull(decoded.get(2));
    }

    @Test
    void rejectsMalformedRecords() {

        assertThrows(
                FileStorageException.class,
                () -> TxtCodec.decodeRecord("ID|Name|Extra", 2)
        );

        assertThrows(
                FileStorageException.class,
                () -> TxtCodec.decodeRecord("ID|%QZ", 2)
        );

        assertThrows(
                FileStorageException.class,
                () -> TxtCodec.decodeRecord("ID|%FF", 2)
        );

        assertThrows(
                FileStorageException.class,
                () -> TxtCodec.decodeRecord("ID|line\nbreak", 2)
        );
    }

    @Test
    void worksWithTxtFileStore(@TempDir Path tempDir)
            throws Exception {

        TxtFileStore store =
                new TxtFileStore(tempDir.toString());

        List<String> original = List.of(
                "DEMO01",
                "Nimal Perera",
                "Java|Python"
        );

        String encoded =
                TxtCodec.encodeRecord(original);

        store.writeRecords(
                "lesson06_demo",
                List.of(encoded)
        );

        // Create a separate store to read from disk.
        TxtFileStore secondStore =
                new TxtFileStore(tempDir.toString());

        ArrayList<String> saved =
                secondStore.readRecords("lesson06_demo");

        assertEquals(1, saved.size());

        ArrayList<String> decoded =
                TxtCodec.decodeRecord(saved.get(0), 3);

        assertEquals(original, decoded);

        String fileText = Files.readString(
                tempDir.resolve("lesson06_demo.txt")
        );

        assertTrue(fileText.startsWith(
                "#SCHEMA|lesson06_demo|1"
        ));

        System.out.println("TXT CODEC STORAGE SUCCESS");
    }
}
