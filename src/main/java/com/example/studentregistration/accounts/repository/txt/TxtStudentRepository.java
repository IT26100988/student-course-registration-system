package com.example.studentregistration.accounts.repository.txt;

import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.accounts.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 * TXT implementation of StudentRepository.
 * Uses ArrayList + BufferedReader/BufferedWriter.
 * File: data/students.txt (UTF-8, one record per line).
 */
@Repository
public class TxtStudentRepository implements StudentRepository {

    private static final String SCHEMA_HEADER = "#SCHEMA|students|1";
    private static final String FILE_NAME = "students.txt";

    private final Path filePath;

    public TxtStudentRepository(@Value("${app.storage.dir:data}") String storageDir) {
        this.filePath = Paths.get(storageDir).resolve(FILE_NAME);
        ensureFileExists();
    }

    // ---------------- interface methods ----------------

    @Override
    public List<Student> findAll() {
        return readAll();
    }

    @Override
    public Optional<Student> findById(String studentId) {
        return readAll().stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst();
    }

    @Override
    public Optional<Student> findByStudentNo(String studentNo) {
        return readAll().stream()
                .filter(s -> s.getStudentNo().equals(studentNo))
                .findFirst();
    }

    @Override
    public Optional<Student> findByEmail(String email) {
        String target = email.toLowerCase();
        return readAll().stream()
                .filter(s -> s.getEmail() != null && s.getEmail().toLowerCase().equals(target))
                .findFirst();
    }

    @Override
    public Student save(Student student) {
        List<Student> all = readAll();
        all.add(student);
        writeAll(all);
        return student;
    }

    @Override
    public Student update(Student student) {
        List<Student> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(student.getId())) {
                all.set(i, student);
                writeAll(all);
                return student;
            }
        }
        throw new IllegalArgumentException("Student not found for update: " + student.getId());
    }

    // ---------------- private helpers (the real TXT + ArrayList work) ----------------

    private List<Student> readAll() {
        List<Student> result = new ArrayList<>();
        if (!Files.exists(filePath)) return result;

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = reader.readLine()) != null) {
                if (first) {
                    first = false;
                    if (!SCHEMA_HEADER.equals(line)) {
                        throw new IllegalStateException("Bad schema header in " + filePath);
                    }
                    continue;
                }
                if (line.isBlank()) continue;
                result.add(parseLine(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed reading " + filePath, e);
        }
        return result;
    }

    private void writeAll(List<Student> students) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(SCHEMA_HEADER);
            writer.newLine();
            for (Student s : students) {
                writer.write(toLine(s));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed writing " + filePath, e);
        }
    }

    private String toLine(Student s) {
        StringBuilder sb = new StringBuilder();
        sb.append(enc(s.getId())).append('|');
        sb.append(enc(s.getStudentNo())).append('|');
        sb.append(enc(s.getFullName())).append('|');
        sb.append(enc(s.getEmail())).append('|');
        sb.append(enc(s.getPasswordHash())).append('|');
        sb.append(enc(s.getStatus().name())).append('|');
        sb.append(enc(s.getCreatedAt().toString())).append('|');
        sb.append(enc(s.getUpdatedAt().toString()));
        return sb.toString();
    }

    private Student parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 8) {
            throw new IllegalStateException("Bad student record (expected 8 fields, got "
                    + parts.length + "): " + line);
        }
        String id = dec(parts[0]);
        String studentNo = dec(parts[1]);
        String fullName = dec(parts[2]);
        String email = dec(parts[3]);
        String passwordHash = dec(parts[4]);
        AccountStatus status = AccountStatus.valueOf(dec(parts[5]));
        Instant createdAt = Instant.parse(dec(parts[6]));
        Instant updatedAt = Instant.parse(dec(parts[7]));

        return new Student(id, studentNo, fullName, email, passwordHash, status, createdAt, updatedAt);
    }

    private static String enc(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static String dec(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private void ensureFileExists() {
        try {
            if (Files.notExists(filePath)) {
                if (filePath.getParent() != null) {
                    Files.createDirectories(filePath.getParent());
                }
                Files.writeString(filePath, SCHEMA_HEADER + System.lineSeparator(),
                        StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot create " + filePath, e);
        }
    }
}
