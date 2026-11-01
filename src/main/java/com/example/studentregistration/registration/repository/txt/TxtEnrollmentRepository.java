
package com.example.studentregistration.registration.repository.txt;

import com.example.studentregistration.registration.model.Enrollment;
import com.example.studentregistration.registration.model.EnrollmentStatus;
import com.example.studentregistration.registration.repository.EnrollmentRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;

@Repository
public class TxtEnrollmentRepository implements EnrollmentRepository {

    private static final String HEADER = "#SCHEMA|enrollments|1";
    private final Path file;

    // Constructor
    public TxtEnrollmentRepository(
            @Value("${app.storage.dir:data}") String storageDir) {

        this.file = Path.of(storageDir)
                .resolve("enrollments.txt").toAbsolutePath();
    }

    // READ - Get all enrollments
    @Override
    public synchronized ArrayList<Enrollment> findAll() {
        return readAll();
    }

    // READ - Search by enrollment ID
    @Override
    public synchronized Optional<Enrollment> findById(String enrollmentId) {

        for (Enrollment enrollment : readAll()) {
            if (enrollment.getEnrollmentId().equals(enrollmentId)) {
                return Optional.of(enrollment);
            }
        }

        return Optional.empty();
    }

    // CREATE - Save a new enrollment
    @Override
    public synchronized void save(Enrollment enrollment) {

        validate(enrollment);
        ArrayList<Enrollment> enrollments = readAll();

        for (Enrollment existing : enrollments) {

            if (existing.getEnrollmentId().equals(enrollment.getEnrollmentId())
                    || existing.getRequestId().equals(enrollment.getRequestId())) {

                throw new IllegalArgumentException(
                        "Duplicate enrollment ID or request ID");
            }
        }

        enrollments.add(enrollment);
        writeAll(enrollments);
    }

    // UPDATE - Update existing enrollment
    @Override
    public synchronized void update(Enrollment enrollment) {

        validate(enrollment);
        ArrayList<Enrollment> enrollments = readAll();

        for (int i = 0; i < enrollments.size(); i++) {

            Enrollment old = enrollments.get(i);

            if (old.getEnrollmentId().equals(enrollment.getEnrollmentId())) {

                // Identity and creation time cannot change
                if (!old.getRequestId().equals(enrollment.getRequestId())
                        || !old.getStudentId().equals(enrollment.getStudentId())
                        || !old.getOfferingId().equals(enrollment.getOfferingId())
                        || !old.getCreatedAt().equals(enrollment.getCreatedAt())) {

                    throw new IllegalArgumentException(
                            "Enrollment identity cannot change");
                }

                enrollments.set(i, enrollment);
                writeAll(enrollments);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Enrollment not found: " + enrollment.getEnrollmentId());
    }

    // Create TXT file with schema header if missing
    private void initializeFile() {

        try {
            Files.createDirectories(file.getParent());

            if (Files.notExists(file)) {

                try (BufferedWriter writer = Files.newBufferedWriter(
                        file,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW)) {

                    writer.write(HEADER);
                    writer.newLine();

                } catch (FileAlreadyExistsException ignored) {
                    // Another call created the file.
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot initialize enrollments file", e);
        }
    }

    // Load TXT records into ArrayList
    private ArrayList<Enrollment> readAll() {

        initializeFile();

        ArrayList<Enrollment> enrollments = new ArrayList<>();
        HashSet<String> ids = new HashSet<>();
        HashSet<String> requestIds = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            if (!HEADER.equals(reader.readLine())) {
                throw new IllegalStateException(
                        "Invalid enrollments schema header");
            }

            String line;
            int lineNo = 1;

            while ((line = reader.readLine()) != null) {
                lineNo++;

                Enrollment enrollment;

                try {
                    enrollment = parse(line);
                    validate(enrollment);

                } catch (RuntimeException e) {
                    throw new IllegalStateException(
                            "Invalid enrollment record at line " + lineNo, e);
                }

                if (!ids.add(enrollment.getEnrollmentId())
                        || !requestIds.add(enrollment.getRequestId())) {

                    throw new IllegalStateException(
                            "Duplicate enrollment/request ID at line " + lineNo);
                }

                enrollments.add(enrollment);
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot read enrollments file", e);
        }

        return enrollments;
    }

    // Convert one TXT line into an Enrollment object
    private Enrollment parse(String line) {

        String[] fields = line.split("\\|", -1);

        if (fields.length != 10) {
            throw new IllegalArgumentException(
                    "Expected exactly 10 fields");
        }

        for (int i = 0; i < fields.length; i++) {

            String decoded = URLDecoder.decode(
                    fields[i], StandardCharsets.UTF_8);

            if (!URLEncoder.encode(decoded, StandardCharsets.UTF_8)
                    .equals(fields[i])) {

                throw new IllegalArgumentException(
                        "Invalid field encoding");
            }

            fields[i] = decoded.isEmpty() ? null : decoded;
        }

        return new Enrollment(
                required(fields[0]),
                required(fields[1]),
                required(fields[2]),
                required(fields[3]),
                EnrollmentStatus.valueOf(required(fields[4])),
                timestamp(required(fields[5])),
                timestamp(fields[6]),
                timestamp(fields[7]),
                timestamp(fields[8]),
                timestamp(required(fields[9]))
        );
    }

    // Rewrite complete ArrayList into TXT
    private void writeAll(ArrayList<Enrollment> enrollments) {

        Path temp = null;

        try {
            temp = Files.createTempFile(
                    file.getParent(), "enrollments_", ".tmp");

            try (BufferedWriter writer = Files.newBufferedWriter(
                    temp, StandardCharsets.UTF_8)) {

                writer.write(HEADER);
                writer.newLine();

                for (Enrollment enrollment : enrollments) {

                    validate(enrollment);

                    writer.write(String.join("|",
                            encode(enrollment.getEnrollmentId()),
                            encode(enrollment.getRequestId()),
                            encode(enrollment.getStudentId()),
                            encode(enrollment.getOfferingId()),
                            encode(enrollment.getStatus()),
                            encode(enrollment.getCreatedAt()),
                            encode(enrollment.getHoldExpiresAt()),
                            encode(enrollment.getActivatedAt()),
                            encode(enrollment.getClosedAt()),
                            encode(enrollment.getUpdatedAt())
                    ));

                    writer.newLine();
                }
            }

            try {
                Files.move(
                        temp,
                        file,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);

            } catch (AtomicMoveNotSupportedException e) {
                Files.move(
                        temp,
                        file,
                        StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot save enrollments file", e);

        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // Best-effort temporary file cleanup.
                }
            }
        }
    }

    // URL encode a field
    private static String encode(Object value) {

        return value == null ? "" : URLEncoder.encode(
                value.toString(), StandardCharsets.UTF_8);
    }

    // Parse an optional UTC timestamp
    private static Instant timestamp(String value) {

        if (value == null) {
            return null;
        }

        if (!value.endsWith("Z")) {
            throw new IllegalArgumentException(
                    "Timestamp must be UTC (Z)");
        }

        return Instant.parse(value);
    }

    // Check mandatory fields
    private static String required(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Required field is missing");
        }

        return value;
    }

    // Validate enrollment
    private static void validate(Enrollment enrollment) {

        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment is null");
        }

        checkId(enrollment.getEnrollmentId(), "ENR");
        checkId(enrollment.getRequestId(), "REQ");
        checkId(enrollment.getStudentId(), "STU");
        checkId(enrollment.getOfferingId(), "OFF");

        if (enrollment.getStatus() == null
                || enrollment.getCreatedAt() == null
                || enrollment.getUpdatedAt() == null) {

            throw new IllegalArgumentException(
                    "Required enrollment field missing");
        }

        if (enrollment.getStatus() == EnrollmentStatus.PENDING_PAYMENT
                && (enrollment.getHoldExpiresAt() == null
                || !enrollment.getHoldExpiresAt()
                .isAfter(enrollment.getCreatedAt()))) {

            throw new IllegalArgumentException(
                    "Valid payment hold expiry required");
        }
    }

    // Validate UUIDv4 identifiers
    private static void checkId(String id, String prefix) {

        if (id == null || !id.matches(prefix
                + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {

            throw new IllegalArgumentException(
                    "Invalid " + prefix + " UUIDv4 ID");
        }
    }
}
