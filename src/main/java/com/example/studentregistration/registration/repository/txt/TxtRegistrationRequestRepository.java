
package com.example.studentregistration.registration.repository.txt;

import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.model.RequestStatus;
import com.example.studentregistration.registration.repository.RegistrationRequestRepository;

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
public class TxtRegistrationRequestRepository
        implements RegistrationRequestRepository {

    private static final String HEADER =
            "#SCHEMA|registration_requests|1";

    private final Path file;

    public TxtRegistrationRequestRepository(
            @Value("${app.storage.dir:data}") String storageDir) {

        this.file = Path.of(
                storageDir, "registration_requests.txt");
    }

    @Override
    public synchronized ArrayList<RegistrationRequest> findAll() {
        return readAll();
    }


    @Override
    public synchronized Optional<RegistrationRequest> findById(
            String requestId) {

        for (RegistrationRequest request : readAll()) {
            if (request.getRequestId().equals(requestId)) {
                return Optional.of(request);
            }
        }

        return Optional.empty();
    }

    @Override
    public synchronized void save(RegistrationRequest request) {

        validate(request);

        ArrayList<RegistrationRequest> requests = readAll();

        for (RegistrationRequest existing : requests) {
            if (existing.getRequestId().equals(request.getRequestId())
                    || existing.getSequenceNo() == request.getSequenceNo()) {

                throw new IllegalArgumentException(
                        "Duplicate request ID or sequence number");
            }
        }

        requests.add(request);
        writeAll(requests);
    }

    @Override
    public synchronized void update(RegistrationRequest request) {

        validate(request);

        ArrayList<RegistrationRequest> requests = readAll();

        for (int i = 0; i < requests.size(); i++) {

            RegistrationRequest old = requests.get(i);

            if (old.getRequestId().equals(request.getRequestId())) {

                if (!old.getStudentId().equals(request.getStudentId())
                        || !old.getOfferingId().equals(request.getOfferingId())
                        || old.getSequenceNo() != request.getSequenceNo()
                        || !old.getRequestedAt().equals(request.getRequestedAt())) {

                    throw new IllegalArgumentException(
                            "Cannot change request identity or FIFO order");
                }

                requests.set(i, request);
                writeAll(requests);
                return;
            }
        }

        throw new IllegalArgumentException(
                "Request not found: " + request.getRequestId());
    }

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
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot initialize registration requests file", e);
        }
    }

    private ArrayList<RegistrationRequest> readAll() {

        initializeFile();

        ArrayList<RegistrationRequest> requests = new ArrayList<>();
        HashSet<String> ids = new HashSet<>();
        HashSet<Long> numbers = new HashSet<>();

        try (BufferedReader reader = Files.newBufferedReader(
                file, StandardCharsets.UTF_8)) {

            if (!HEADER.equals(reader.readLine())) {
                throw new IllegalStateException(
                        "Invalid registration requests schema header");
            }

            String line;
            int lineNo = 1;

            while ((line = reader.readLine()) != null) {
                lineNo++;

                RegistrationRequest request;

                try {
                    request = parse(line);
                    validate(request);

                } catch (RuntimeException e) {
                    throw new IllegalStateException(
                            "Malformed record at line " + lineNo, e);
                }

                if (!ids.add(request.getRequestId())
                        || !numbers.add(request.getSequenceNo())) {

                    throw new IllegalStateException(
                            "Duplicate ID or sequence at line " + lineNo);
                }

                requests.add(request);
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot read registration requests", e);
        }

        return requests;
    }

    private RegistrationRequest parse(String line) {

        String[] parts = line.split("\\|", -1);

        if (parts.length != 10) {
            throw new IllegalArgumentException(
                    "Expected 10 fields, got " + parts.length);
        }

        for (int i = 0; i < parts.length; i++) {

            String decoded = URLDecoder.decode(
                    parts[i], StandardCharsets.UTF_8);

            if (!URLEncoder.encode(decoded, StandardCharsets.UTF_8)
                    .equals(parts[i])) {

                throw new IllegalArgumentException(
                        "Invalid URL-encoded field");
            }

            parts[i] = decoded.isEmpty() ? null : decoded;
        }

        return new RegistrationRequest(
                required(parts[0]),
                required(parts[1]),
                required(parts[2]),
                Long.parseLong(required(parts[3])),
                RequestStatus.valueOf(required(parts[4])),
                parseInstant(required(parts[5])),
                parseInstant(parts[6]),
                parts[7],
                parts[8],
                parseInstant(parts[9])
        );
    }

    private void writeAll(ArrayList<RegistrationRequest> requests) {

        Path temp = null;

        try {
            temp = Files.createTempFile(
                    file.getParent(), "reg_requests_", ".tmp");

            try (BufferedWriter writer = Files.newBufferedWriter(
                    temp, StandardCharsets.UTF_8)) {

                writer.write(HEADER);
                writer.newLine();

                for (RegistrationRequest request : requests) {

                    validate(request);

                    writer.write(String.join("|",
                            encode(request.getRequestId()),
                            encode(request.getStudentId()),
                            encode(request.getOfferingId()),
                            encode(request.getSequenceNo()),
                            encode(request.getStatus()),
                            encode(request.getRequestedAt()),
                            encode(request.getProcessedAt()),
                            encode(request.getProcessedByAdminId()),
                            encode(request.getDecisionReason()),
                            encode(request.getCancelledAt())
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
                    "Cannot save registration requests", e);

        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // Temporary cleanup is best-effort.
                }
            }
        }
    }

    private static String encode(Object value) {

        return value == null
                ? ""
                : URLEncoder.encode(
                value.toString(), StandardCharsets.UTF_8);
    }

    private static Instant parseInstant(String value) {

        if (value == null) {
            return null;
        }

        if (!value.endsWith("Z")) {
            throw new IllegalArgumentException(
                    "Timestamp must use UTC Z format");
        }

        return Instant.parse(value);
    }

    private static String required(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Missing mandatory field");
        }

        return value;
    }

    private static void validate(RegistrationRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request is null");
        }

        checkId(request.getRequestId(), "REQ");
        checkId(request.getStudentId(), "STU");
        checkId(request.getOfferingId(), "OFF");

        if (request.getSequenceNo() <= 0
                || request.getStatus() == null
                || request.getRequestedAt() == null) {

            throw new IllegalArgumentException(
                    "Invalid required request fields");
        }
    }

    private static void checkId(String id, String prefix) {

        if (id == null || !id.matches(prefix
                + "_[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {

            throw new IllegalArgumentException(
                    "Invalid " + prefix + " UUIDv4 ID");
        }
    }
}
