package com.example.studentregistration.accounts.repository.txt;

import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Admin;
import com.example.studentregistration.accounts.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TxtAdminRepository implements AdminRepository {

    private static final String SCHEMA_HEADER = "#SCHEMA|admins|1";
    private static final String FILE_NAME = "admins.txt";

    private final Path filePath;

    public TxtAdminRepository(@Value("${app.storage.dir:data}") String storageDir) {
        this.filePath = Paths.get(storageDir).resolve(FILE_NAME);
        ensureFileExists();
    }

    @Override
    public List<Admin> findAll() {
        return readAll();
    }

    @Override
    public Optional<Admin> findById(String adminId) {
        return readAll().stream()
                .filter(a -> a.getId().equals(adminId))
                .findFirst();
    }

    @Override
    public Optional<Admin> findByEmail(String email) {
        String target = email.toLowerCase();
        return readAll().stream()
                .filter(a -> a.getEmail() != null && a.getEmail().toLowerCase().equals(target))
                .findFirst();
    }

    @Override
    public Admin save(Admin admin) {
        List<Admin> all = readAll();
        all.add(admin);
        writeAll(all);
        return admin;
    }

    @Override
    public Admin update(Admin admin) {
        List<Admin> all = readAll();
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(admin.getId())) {
                all.set(i, admin);
                writeAll(all);
                return admin;
            }
        }
        throw new IllegalArgumentException("Admin not found for update: " + admin.getId());
    }

    @Override
    public boolean existsAnyActiveAdmin() {
        return readAll().stream().anyMatch(a -> a.getStatus() == AccountStatus.ACTIVE);
    }

    // ---------------- private helpers ----------------

    private List<Admin> readAll() {
        List<Admin> result = new ArrayList<>();
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

    private void writeAll(List<Admin> admins) {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
            writer.write(SCHEMA_HEADER);
            writer.newLine();
            for (Admin a : admins) {
                writer.write(toLine(a));
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed writing " + filePath, e);
        }
    }

    private String toLine(Admin a) {
        StringBuilder sb = new StringBuilder();
        sb.append(enc(a.getId())).append('|');
        sb.append(enc(a.getFullName())).append('|');
        sb.append(enc(a.getEmail())).append('|');
        sb.append(enc(a.getPasswordHash())).append('|');
        sb.append(enc(a.getStatus().name())).append('|');
        sb.append(enc(a.getCreatedAt().toString())).append('|');
        sb.append(enc(a.getUpdatedAt().toString()));
        return sb.toString();
    }

    private Admin parseLine(String line) {
        String[] parts = line.split("\\|", -1);
        if (parts.length != 7) {
            throw new IllegalStateException("Bad admin record (expected 7 fields, got "
                    + parts.length + "): " + line);
        }
        String id = dec(parts[0]);
        String fullName = dec(parts[1]);
        String email = dec(parts[2]);
        String passwordHash = dec(parts[3]);
        AccountStatus status = AccountStatus.valueOf(dec(parts[4]));
        Instant createdAt = Instant.parse(dec(parts[5]));
        Instant updatedAt = Instant.parse(dec(parts[6]));

        return new Admin(id, fullName, email, passwordHash, status, createdAt, updatedAt);
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
