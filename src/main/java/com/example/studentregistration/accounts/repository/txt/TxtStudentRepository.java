package com.example.studentregistration.accounts.repository.txt;

import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.model.Student;
import com.example.studentregistration.accounts.repository.StudentRepository;
import com.example.studentregistration.shared.exception.FileStorageException;
import com.example.studentregistration.shared.filehandling.TxtCodec;
import com.example.studentregistration.shared.filehandling.TxtFileStore;
import com.example.studentregistration.shared.filehandling.StorageLockManager;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class TxtStudentRepository implements StudentRepository {

    private final TxtFileStore txtFileStore;
    private final StorageLockManager storageLockManager;

    public TxtStudentRepository(
            TxtFileStore txtFileStore,
            StorageLockManager storageLockManager) {

        this.txtFileStore = txtFileStore;
        this.storageLockManager = storageLockManager;
    }

    // Create
    @Override
    public void save(Student student) throws FileStorageException {

        storageLockManager.withFileLock("students", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("students");

            for (String record : records) {

                Student existingStudent = convertToStudent(record);

                if (existingStudent.getId().equals(student.getId())
                        || existingStudent.getStudentNo().equalsIgnoreCase(student.getStudentNo())
                        || existingStudent.getEmail().equalsIgnoreCase(student.getEmail())) {

                    throw new FileStorageException("Student already exists");
                }
            }

            records.add(convertToRecord(student));

            txtFileStore.writeRecords("students", records);

            return null;
        });
    }

    // Find by ID
    @Override
    public Optional<Student> findById(String id)
            throws FileStorageException {

        return storageLockManager.withFileLock("students", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("students");

            for (String record : records) {

                Student student = convertToStudent(record);

                if (student.getId().equals(id)) {
                    return Optional.of(student);
                }
            }

            return Optional.empty();
        });
    }

    // Find by Email
    @Override
    public Optional<Student> findByEmail(String email)
            throws FileStorageException {

        return storageLockManager.withFileLock("students", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("students");

            for (String record : records) {

                Student student = convertToStudent(record);

                if (student.getEmail().equalsIgnoreCase(email)) {
                    return Optional.of(student);
                }
            }

            return Optional.empty();
        });
    }

    // Read all
    @Override
    public List<Student> findAll() throws FileStorageException {

        return storageLockManager.withFileLock("students", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("students");

            List<Student> students = new ArrayList<>();

            for (String record : records) {
                students.add(convertToStudent(record));
            }

            return students;
        });
    }

    // Update
    @Override
    public boolean update(String id, Student updatedStudent)
            throws FileStorageException {

        return storageLockManager.withFileLock("students", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("students");

            boolean found = false;

            for (int i = 0; i < records.size(); i++) {

                Student student = convertToStudent(records.get(i));

                if (student.getId().equals(id)) {

                    records.set(i, convertToRecord(updatedStudent));
                    found = true;
                    break;
                }
            }

            if (found) {
                txtFileStore.writeRecords("students", records);
            }

            return found;
        });
    }

    // Delete
    @Override
    public boolean deleteById(String id)
            throws FileStorageException {

        return storageLockManager.withFileLock("students", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("students");

            boolean removed = false;

            for (int i = 0; i < records.size(); i++) {

                Student student = convertToStudent(records.get(i));

                if (student.getId().equals(id)) {

                    records.remove(i);
                    removed = true;
                    break;
                }
            }

            if (removed) {
                txtFileStore.writeRecords("students", records);
            }

            return removed;
        });
    }

    // Check duplicate Student Number
    @Override
    public boolean existsByStudentNo(String studentNo)
            throws FileStorageException {

        return storageLockManager.withFileLock("students", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("students");

            for (String record : records) {

                Student student = convertToStudent(record);

                if (student.getStudentNo().equalsIgnoreCase(studentNo)) {
                    return true;
                }
            }

            return false;
        });
    }

    // Check duplicate Email
    @Override
    public boolean existsByEmail(String email)
            throws FileStorageException {

        return storageLockManager.withFileLock("students", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("students");

            for (String record : records) {

                Student student = convertToStudent(record);

                if (student.getEmail().equalsIgnoreCase(email)) {
                    return true;
                }
            }

            return false;
        });
    }

    // Convert Student object to TXT record
    private String convertToRecord(Student student)
            throws FileStorageException {

        List<String> fields = Arrays.asList(
                student.getId(),
                student.getStudentNo(),
                student.getFullName(),
                student.getEmail(),
                student.getPasswordHash(),
                student.getStatus().name(),
                student.getCreatedAt(),
                student.getUpdatedAt()
        );

        return TxtCodec.encodeRecord(fields);
    }

    // Convert TXT record to Student object
    private Student convertToStudent(String record)
            throws FileStorageException {

        List<String> fields = TxtCodec.decodeRecord(record, 8);

        return new Student(
                fields.get(0),
                fields.get(2),
                fields.get(3),
                fields.get(4),
                AccountStatus.valueOf(fields.get(5)),
                fields.get(6),
                fields.get(7),
                fields.get(1)
        );
    }
}