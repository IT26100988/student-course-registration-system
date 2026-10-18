
package com.example.studentregistration.shared.filehandling;

import com.example.studentregistration.shared.exception.FileStorageException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Lesson07NoteStore {

    private static final String FILE_NAME = "lesson07_notes";

    private final TxtFileStore fileStore;

    public Lesson07NoteStore(Path directory) {
        this.fileStore = new TxtFileStore(directory.toString());
    }

    // CREATE - Add a new note
    public void create(String id, String text)
            throws FileStorageException {

        validateText(text);

        ArrayList<String> records =
                fileStore.readRecords(FILE_NAME);

        if (findIndex(records, id) != -1) {
            throw new IllegalArgumentException("Duplicate note ID");
        }

        String newRecord =
                TxtCodec.encodeRecord(List.of(id, text));

        records.add(newRecord);

        fileStore.writeRecords(FILE_NAME, records);
    }

    // READ - Find a note by ID
    public String read(String id)
            throws FileStorageException {

        ArrayList<String> records =
                fileStore.readRecords(FILE_NAME);

        int index = findIndex(records, id);

        if (index == -1) {
            throw new NoSuchElementException("Note not found: " + id);
        }

        return TxtCodec.decodeRecord(
                records.get(index), 2
        ).get(1);
    }

    // UPDATE - Change an existing note
    public void update(String id, String newText)
            throws FileStorageException {

        validateText(newText);

        ArrayList<String> records =
                fileStore.readRecords(FILE_NAME);

        int index = findIndex(records, id);

        if (index == -1) {
            throw new NoSuchElementException("Note not found: " + id);
        }

        String updatedRecord =
                TxtCodec.encodeRecord(List.of(id, newText));

        records.set(index, updatedRecord);

        fileStore.writeRecords(FILE_NAME, records);
    }

    // DELETE - Remove a practice note
    public void delete(String id)
            throws FileStorageException {

        ArrayList<String> records =
                fileStore.readRecords(FILE_NAME);

        int index = findIndex(records, id);

        if (index == -1) {
            throw new NoSuchElementException("Note not found: " + id);
        }

        records.remove(index);

        fileStore.writeRecords(FILE_NAME, records);
    }

    // List all note IDs
    public ArrayList<String> listIds()
            throws FileStorageException {

        ArrayList<String> records =
                fileStore.readRecords(FILE_NAME);

        ArrayList<String> ids = new ArrayList<>();

        for (String record : records) {
            ids.add(TxtCodec.decodeRecord(record, 2).get(0));
        }

        return ids;
    }

    // Search ArrayList for an ID
    private int findIndex(ArrayList<String> records, String id)
            throws FileStorageException {

        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Note ID is required");
        }

        for (int i = 0; i < records.size(); i++) {

            ArrayList<String> fields =
                    TxtCodec.decodeRecord(records.get(i), 2);

            if (id.equals(fields.get(0))) {
                return i;
            }
        }

        return -1;
    }

    private void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Note text is required");
        }
    }
}
