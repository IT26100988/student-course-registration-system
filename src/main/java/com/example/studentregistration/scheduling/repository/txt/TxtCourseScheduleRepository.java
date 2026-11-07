package com.example.studentregistration.scheduling.repository.txt;

import com.example.studentregistration.scheduling.model.CourseSchedule;
import com.example.studentregistration.scheduling.model.RecordStatus;
import com.example.studentregistration.scheduling.repository.CourseScheduleRepository;
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
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Repository
public class TxtCourseScheduleRepository implements CourseScheduleRepository {
    private static final String HEADER = "#SCHEMA|course_schedules|1";
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    private final Path filePath;

    public TxtCourseScheduleRepository(
            @Value("${app.storage.dir:data}") String storageDirectory) {
        this.filePath = Path.of(storageDirectory, "course_schedules.txt");
    }

    @Override
    public synchronized ArrayList<CourseSchedule> findAll() throws IOException {
        ensureFileExists();
        return readAll();
    }

    @Override
    public synchronized CourseSchedule findById(String scheduleId)
            throws IOException {
        for (CourseSchedule schedule : findAll()) {
            if (schedule.getScheduleId().equals(scheduleId)) {
                return schedule;
            }
        }
        return null;
    }

    @Override
    public synchronized void save(CourseSchedule schedule) throws IOException {
        validate(schedule);

        ArrayList<CourseSchedule> schedules = findAll();

        for (CourseSchedule existing : schedules) {
            if (existing.getScheduleId().equals(schedule.getScheduleId())) {
                throw new IOException("Schedule ID already exists.");
            }
        }

        Instant now = Instant.now();

        if (schedule.getCreatedAt() == null) {
            schedule.setCreatedAt(now);
        }
        schedule.setUpdatedAt(now);

        schedules.add(schedule);
        writeAll(schedules);
    }

    @Override
    public synchronized void update(CourseSchedule schedule) throws IOException {
        validate(schedule);

        ArrayList<CourseSchedule> schedules = findAll();
        int foundIndex = -1;

        for (int i = 0; i < schedules.size(); i++) {
            CourseSchedule existing = schedules.get(i);

            if (existing.getScheduleId().equals(schedule.getScheduleId())) {
                foundIndex = i;
                schedule.setCreatedAt(existing.getCreatedAt());
            }
        }

        if (foundIndex == -1) {
            throw new IOException("Schedule was not found.");
        }

        schedule.setUpdatedAt(Instant.now());
        schedules.set(foundIndex, schedule);
        writeAll(schedules);
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

    private ArrayList<CourseSchedule> readAll() throws IOException {
        ArrayList<CourseSchedule> schedules = new ArrayList<>();

        try (BufferedReader reader =
                     Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();

            if (!HEADER.equals(header)) {
                throw new IOException("Invalid course_schedules.txt header.");
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] fields = line.split("\\|", -1);

                if (fields.length != 11) {
                    throw new IOException(
                            "Invalid schedule record at line " + lineNumber);
                }

                try {
                    CourseSchedule schedule = new CourseSchedule(
                            decode(fields[0]),
                            decode(fields[1]),
                            decode(fields[2]),
                            DayOfWeek.valueOf(decode(fields[3])),
                            LocalTime.parse(decode(fields[4]), TIME_FORMAT),
                            LocalTime.parse(decode(fields[5]), TIME_FORMAT),
                            decode(fields[6]),
                            decode(fields[7]),
                            RecordStatus.valueOf(decode(fields[8])),
                            Instant.parse(decode(fields[9])),
                            Instant.parse(decode(fields[10]))
                    );

                    validate(schedule);
                    schedules.add(schedule);
                } catch (RuntimeException error) {
                    throw new IOException(
                            "Invalid schedule record at line " + lineNumber,
                            error);
                }
            }
        }

        return schedules;
    }

    private void writeAll(ArrayList<CourseSchedule> schedules)
            throws IOException {
        Path temporaryFile = Files.createTempFile(
                filePath.getParent(), "schedules-", ".tmp");

        try {
            try (BufferedWriter writer =
                         Files.newBufferedWriter(
                                 temporaryFile, StandardCharsets.UTF_8)) {
                writer.write(HEADER);
                writer.newLine();

                for (CourseSchedule schedule : schedules) {
                    writer.write(
                            encode(schedule.getScheduleId()) + "|"
                                    + encode(schedule.getOfferingId()) + "|"
                                    + encode(schedule.getLecturerId()) + "|"
                                    + encode(schedule.getDayOfWeek().name()) + "|"
                                    + encode(schedule.getStartLocal()
                                    .format(TIME_FORMAT)) + "|"
                                    + encode(schedule.getEndLocal()
                                    .format(TIME_FORMAT)) + "|"
                                    + encode(schedule.getTimeZone()) + "|"
                                    + encode(schedule.getRoom()) + "|"
                                    + encode(schedule.getStatus().name()) + "|"
                                    + encode(schedule.getCreatedAt().toString())
                                    + "|"
                                    + encode(schedule.getUpdatedAt().toString())
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

    private void validate(CourseSchedule schedule) throws IOException {
        if (schedule == null
                || isBlank(schedule.getScheduleId())
                || isBlank(schedule.getOfferingId())
                || isBlank(schedule.getLecturerId())
                || schedule.getDayOfWeek() == null
                || schedule.getStartLocal() == null
                || schedule.getEndLocal() == null
                || !schedule.getStartLocal().isBefore(schedule.getEndLocal())
                || !"Asia/Colombo".equals(schedule.getTimeZone())
                || isBlank(schedule.getRoom())
                || schedule.getStatus() == null) {
            throw new IOException("Schedule fields are invalid.");
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