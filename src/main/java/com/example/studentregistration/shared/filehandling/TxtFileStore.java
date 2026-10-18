
package com.example.studentregistration.shared.filehandling;

import com.example.studentregistration.shared.exception.FileStorageException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Component
public class TxtFileStore {

    private final Path directory;

    public TxtFileStore(
            @Value("${app.storage.dir:data}") String folder) {

        this.directory = Path.of(folder)
                .toAbsolutePath()
                .normalize();
    }

    private Path filePath(String fileStem)
            throws FileStorageException {

        if (fileStem == null ||
                !fileStem.matches("[a-z][a-z0-9_]*")) {

            throw new FileStorageException(
                    "Invalid TXT filename");
        }

        return directory.resolve(fileStem + ".txt");
    }

    private String header(String fileStem) {
        return "#SCHEMA|" + fileStem + "|1";
    }

    private void ensureFile(
            Path path, String fileStem) throws IOException {

        Files.createDirectories(directory);

        if (Files.notExists(path)) {

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 path,
                                 StandardCharsets.UTF_8,
                                 StandardOpenOption.CREATE_NEW)) {

                writer.write(header(fileStem));
                writer.write('\n');
            }
        }
    }

    public synchronized ArrayList<String> readRecords(
            String fileStem) throws FileStorageException {

        Path path = filePath(fileStem);

        try {
            ensureFile(path, fileStem);

            try (BufferedReader reader =
                         Files.newBufferedReader(
                                 path,
                                 StandardCharsets.UTF_8)) {

                String actualHeader = reader.readLine();

                if (!header(fileStem).equals(actualHeader)) {
                    throw new FileStorageException(
                            "Invalid TXT schema header: " + fileStem);
                }

                ArrayList<String> records = new ArrayList<>();
                String line;

                while ((line = reader.readLine()) != null) {

                    if (line.isBlank()) {
                        throw new FileStorageException(
                                "Blank TXT record: " + fileStem);
                    }

                    records.add(line);
                }

                return records;
            }

        } catch (IOException e) {
            throw new FileStorageException(
                    "Cannot read TXT file: " + fileStem, e);
        }
    }

    // NEW IN STEP 12:
    // Check that the file ends with a newline character.
    public synchronized void requireTrailingNewline(
            String fileStem) throws FileStorageException {

        Path path = filePath(fileStem);

        try (FileChannel channel = FileChannel.open(
                path, StandardOpenOption.READ)) {

            long length = channel.size();

            if (length == 0) {
                throw new FileStorageException(
                        "TXT file is empty: " + fileStem);
            }

            ByteBuffer lastByte = ByteBuffer.allocate(1);

            int count = channel.read(
                    lastByte, length - 1);

            if (count != 1 ||
                    lastByte.array()[0] != (byte) '\n') {

                throw new FileStorageException(
                        "TXT file is missing its final newline: "
                                + fileStem);
            }

        } catch (IOException e) {
            throw new FileStorageException(
                    "Cannot verify TXT file ending: "
                            + fileStem, e);
        }
    }

    public synchronized void writeRecords(
            String fileStem,
            List<String> records)
            throws FileStorageException {

        Path path = filePath(fileStem);

        if (records == null) {
            throw new FileStorageException(
                    "Records cannot be null");
        }

        for (String record : records) {

            if (record == null ||
                    record.isBlank() ||
                    record.contains("\n") ||
                    record.contains("\r")) {

                throw new FileStorageException(
                        "Invalid TXT record");
            }
        }

        readRecords(fileStem);

        Path temp = null;

        try {
            temp = Files.createTempFile(
                    directory,
                    fileStem + "-",
                    ".tmp");

            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 temp,
                                 StandardCharsets.UTF_8)) {

                writer.write(header(fileStem));
                writer.write('\n');

                for (String record : records) {
                    writer.write(record);
                    writer.write('\n');
                }
            }

            try {
                Files.move(
                        temp, path,
                        ATOMIC_MOVE,
                        REPLACE_EXISTING);

            } catch (AtomicMoveNotSupportedException e) {

                Files.move(
                        temp, path,
                        REPLACE_EXISTING);
            }

        } catch (IOException e) {
            throw new FileStorageException(
                    "Cannot write TXT file: " + fileStem, e);

        } finally {

            if (temp != null) {

                try {
                    Files.deleteIfExists(temp);

                } catch (IOException ignored) {
                    // Cleanup may be retried during maintenance.
                }
            }
        }
    }
}
