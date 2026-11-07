package com.example.studentregistration.scheduling.repository.txt;

import com.example.studentregistration.scheduling.model.Prerequisite;
import com.example.studentregistration.scheduling.model.RecordStatus;
import com.example.studentregistration.scheduling.repository.PrerequisiteRepository;
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
public class TxtPrerequisiteRepository implements PrerequisiteRepository {
    private static final String HEADER = "#SCHEMA|prerequisites|1";

    private final Path filePath;

    public TxtPrerequisiteRepository(
            @Value("${app.storage.dir:data}") String storageDirectory) {
        this.filePath = Path.of(storageDirectory, "prerequisites.txt");
    }

    @Override
    public synchronized ArrayList<Prerequisite> findAll() throws IOException {
        ensureFileExists();
        return readAll();
    }

    @Override
    public synchronized Prerequisite findById(String prerequisiteId)
            throws IOException {
        for (Prerequisite prerequisite : findAll()) {
            if (prerequisite.getPrerequisiteId().equals(prerequisiteId)) {
                return prerequisite;
            }
        }
        return null;
    }

    @Override
    public synchronized void save(Prerequisite prerequisite) throws IOException {
        validate(prerequisite);

        ArrayList<Prerequisite> prerequisites = findAll();

        for (Prerequisite existing : prerequisites) {
            if (existing.getPrerequisiteId()
                    .equals(prerequisite.getPrerequisiteId())) {
                throw new IOException("Prerequisite ID already exists.");
            }
        }

        ensureNoDuplicateActivePair(prerequisites, prerequisite, null);

        Instant now = Instant.now();

        if (prerequisite.getCreatedAt() == null) {
            prerequisite.setCreatedAt(now);
        }
        prerequisite.setUpdatedAt(now);

        prerequisites.add(prerequisite);
        writeAll(prerequisites);
    }

    @Override
    public synchronized void update(Prerequisite prerequisite)
            throws IOException {
        validate(prerequisite);

        ArrayList<Prerequisite> prerequisites = findAll();
        int foundIndex = -1;

        for (int i = 0; i < prerequisites.size(); i++) {
            Prerequisite existing = prerequisites.get(i);

            if (existing.getPrerequisiteId()
                    .equals(prerequisite.getPrerequisiteId())) {
                foundIndex = i;
                prerequisite.setCreatedAt(existing.getCreatedAt());
            }
        }

        if (foundIndex == -1) {
            throw new IOException("Prerequisite was not found.");
        }

        ensureNoDuplicateActivePair(
                prerequisites,
                prerequisite,
                prerequisite.getPrerequisiteId()
        );

        prerequisite.setUpdatedAt(Instant.now());
        prerequisites.set(foundIndex, prerequisite);
        writeAll(prerequisites);
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

    private ArrayList<Prerequisite> readAll() throws IOException {
        ArrayList<Prerequisite> prerequisites = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();

            if (!HEADER.equals(header)) {
                throw new IOException("Invalid prerequisites.txt header.");
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split("\\|", -1);

                if (fields.length != 6) {
                    throw new IOException(
                            "Invalid prerequisite record at line " + lineNumber);
                }

                try {
                    Prerequisite prerequisite = new Prerequisite(
                            decode(fields[0]),
                            decode(fields[1]),
                            decode(fields[2]),
                            RecordStatus.valueOf(decode(fields[3])),
                            Instant.parse(decode(fields[4])),
                            Instant.parse(decode(fields[5]))
                    );

                    validate(prerequisite);
                    prerequisites.add(prerequisite);
                } catch (RuntimeException error) {
                    throw new IOException(
                            "Invalid prerequisite record at line " + lineNumber,
                            error);
                }
            }
        }

        return prerequisites;
    }

    private void writeAll(ArrayList<Prerequisite> prerequisites)
            throws IOException {
        Path temporaryFile = Files.createTempFile(
                filePath.getParent(), "prerequisites-", ".tmp");

        try {
            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 temporaryFile, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();

                for (Prerequisite prerequisite : prerequisites) {
                    writer.write(
                            encode(prerequisite.getPrerequisiteId()) + "|"
                                    + encode(prerequisite.getCourseId()) + "|"
                                    + encode(prerequisite.getRequiredCourseId()) + "|"
                                    + encode(prerequisite.getStatus().name()) + "|"
                                    + encode(prerequisite.getCreatedAt().toString())
                                    + "|"
                                    + encode(prerequisite.getUpdatedAt().toString())
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

    private void validate(Prerequisite prerequisite) throws IOException {
        if (prerequisite == null
                || isBlank(prerequisite.getPrerequisiteId())
                || isBlank(prerequisite.getCourseId())
                || isBlank(prerequisite.getRequiredCourseId())
                || prerequisite.getStatus() == null) {
            throw new IOException("Prerequisite fields cannot be blank.");
        }

        if (prerequisite.getCourseId()
                .equals(prerequisite.getRequiredCourseId())) {
            throw new IOException(
                    "A course cannot be its own prerequisite.");
        }
    }

    private void ensureNoDuplicateActivePair(
            ArrayList<Prerequisite> prerequisites,
            Prerequisite candidate,
            String excludedId) throws IOException {
        if (candidate.getStatus() != RecordStatus.ACTIVE) {
            return;
        }

        for (Prerequisite existing : prerequisites) {
            boolean isSameRecord = existing.getPrerequisiteId()
                    .equals(excludedId);

            boolean isSameActivePair =
                    existing.getStatus() == RecordStatus.ACTIVE
                            && existing.getCourseId()
                            .equals(candidate.getCourseId())
                            && existing.getRequiredCourseId()
                            .equals(candidate.getRequiredCourseId());

            if (!isSameRecord && isSameActivePair) {
                throw new IOException(
                        "This active prerequisite rule already exists.");
            }
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