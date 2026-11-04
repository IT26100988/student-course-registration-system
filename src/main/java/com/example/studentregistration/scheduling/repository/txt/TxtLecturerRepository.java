package com.example.studentregistration.scheduling.repository.txt;

import com.example.studentregistration.scheduling.model.Lecturer;
import com.example.studentregistration.scheduling.model.RecordStatus;
import com.example.studentregistration.scheduling.repository.LecturerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.ArrayList;

@Repository
public class TxtLecturerRepository implements LecturerRepository {
    private static final String HEADER = "#SCHEMA|lecturers|1";

    private final Path filePath;

    public TxtLecturerRepository(
            @Value("${app.storage.dir:data}") String storageDirectory) {
        this.filePath = Path.of(storageDirectory, "lecturers.txt");
    }

    @Override
    public synchronized ArrayList<Lecturer> findAll() throws IOException {
        ensureFileExists();
        return readAll();
    }

    @Override
    public synchronized Lecturer findById(String lecturerId) throws IOException {
        for (Lecturer lecturer : findAll()) {
            if (lecturer.getLecturerId().equals(lecturerId)) {
                return lecturer;
            }
        }
        return null;
    }

    @Override
    public synchronized void save(Lecturer lecturer) throws IOException {
        validate(lecturer);

        ArrayList<Lecturer> lecturers = findAll();

        for (Lecturer existing : lecturers) {
            if (existing.getLecturerId().equals(lecturer.getLecturerId())) {
                throw new IOException("Lecturer ID already exists.");
            }
            if (existing.getEmail().equalsIgnoreCase(lecturer.getEmail())) {
                throw new IOException("Lecturer email already exists.");
            }
        }

        Instant now = Instant.now();

        if (lecturer.getCreatedAt() == null) {
            lecturer.setCreatedAt(now);
        }
        lecturer.setUpdatedAt(now);

        lecturers.add(lecturer);
        writeAll(lecturers);
    }

    @Override
    public synchronized void update(Lecturer lecturer) throws IOException {
        validate(lecturer);

        ArrayList<Lecturer> lecturers = findAll();
        int foundIndex = -1;

        for (int i = 0; i < lecturers.size(); i++) {
            Lecturer existing = lecturers.get(i);

            if (!existing.getLecturerId().equals(lecturer.getLecturerId())
                    && existing.getEmail().equalsIgnoreCase(lecturer.getEmail())) {
                throw new IOException("Lecturer email already exists.");
            }

            if (existing.getLecturerId().equals(lecturer.getLecturerId())) {
                foundIndex = i;
                lecturer.setCreatedAt(existing.getCreatedAt());
            }
        }

        if (foundIndex == -1) {
            throw new IOException("Lecturer was not found.");
        }

        lecturer.setUpdatedAt(Instant.now());
        lecturers.set(foundIndex, lecturer);
        writeAll(lecturers);
    }

    private void ensureFileExists() throws IOException {
        Path directory = filePath.getParent();
        Files.createDirectories(directory);

        if (!Files.exists(filePath)) {
            try (BufferedWriter writer = Files.newBufferedWriter(
                    filePath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.WRITE)) {
                writer.write(HEADER);
                writer.newLine();
            }
        }
    }

    private ArrayList<Lecturer> readAll() throws IOException {
        ArrayList<Lecturer> lecturers = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();

            if (!HEADER.equals(header)) {
                throw new IOException("Invalid lecturers.txt header.");
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split("\\|", -1);

                if (fields.length != 7) {
                    throw new IOException(
                            "Invalid lecturer record at line " + lineNumber);
                }

                try {
                    Lecturer lecturer = new Lecturer(
                            decode(fields[0]),
                            decode(fields[1]),
                            decode(fields[2]),
                            decode(fields[3]),
                            RecordStatus.valueOf(decode(fields[4])),
                            Instant.parse(decode(fields[5])),
                            Instant.parse(decode(fields[6]))
                    );

                    validate(lecturer);
                    lecturers.add(lecturer);
                } catch (RuntimeException error) {
                    throw new IOException(
                            "Invalid lecturer record at line " + lineNumber,
                            error);
                }
            }
        }

        return lecturers;
    }

    private void writeAll(ArrayList<Lecturer> lecturers) throws IOException {
        Path temporaryFile = Files.createTempFile(
                filePath.getParent(), "lecturers-", ".tmp");

        try {
            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 temporaryFile, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();

                for (Lecturer lecturer : lecturers) {
                    writer.write(
                            encode(lecturer.getLecturerId()) + "|"
                                    + encode(lecturer.getFullName()) + "|"
                                    + encode(lecturer.getEmail()) + "|"
                                    + encode(lecturer.getDepartment()) + "|"
                                    + encode(lecturer.getStatus().name()) + "|"
                                    + encode(lecturer.getCreatedAt().toString()) + "|"
                                    + encode(lecturer.getUpdatedAt().toString())
                    );
                    writer.newLine();
                }
            }

            try {
                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (AtomicMoveNotSupportedException error) {
                Files.move(
                        temporaryFile,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private void validate(Lecturer lecturer) throws IOException {
        if (lecturer == null
                || isBlank(lecturer.getLecturerId())
                || isBlank(lecturer.getFullName())
                || isBlank(lecturer.getEmail())
                || isBlank(lecturer.getDepartment())
                || lecturer.getStatus() == null) {
            throw new IOException("Lecturer fields cannot be blank.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}