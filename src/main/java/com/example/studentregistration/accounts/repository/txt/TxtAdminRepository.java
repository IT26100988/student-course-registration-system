package com.example.studentregistration.accounts.repository.txt;

import com.example.studentregistration.accounts.model.Admin;
import com.example.studentregistration.accounts.model.AccountStatus;
import com.example.studentregistration.accounts.repository.AdminRepository;

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
public class TxtAdminRepository implements AdminRepository {

    private final TxtFileStore txtFileStore;
    private final StorageLockManager storageLockManager;

    public TxtAdminRepository(
            TxtFileStore txtFileStore,
            StorageLockManager storageLockManager) {

        this.txtFileStore = txtFileStore;
        this.storageLockManager = storageLockManager;
    }

    @Override
    public void save(Admin admin) throws FileStorageException {

        storageLockManager.withFileLock("admins", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("admins");

            for (String record : records) {

                Admin existingAdmin = convertToAdmin(record);

                if (existingAdmin.getAdminId().equals(admin.getAdminId())
                        || existingAdmin.getEmail().equalsIgnoreCase(admin.getEmail())) {

                    throw new FileStorageException("Admin already exists");
                }
            }

            records.add(convertToRecord(admin));

            txtFileStore.writeRecords("admins", records);

            return null;
        });
    }

    @Override
    public Optional<Admin> findById(String id)
            throws FileStorageException {

        return storageLockManager.withFileLock("admins", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("admins");

            for (String record : records) {

                Admin admin = convertToAdmin(record);

                if (admin.getAdminId().equals(id)) {
                    return Optional.of(admin);
                }
            }

            return Optional.empty();
        });
    }

    @Override
    public List<Admin> findAll() throws FileStorageException {

        return storageLockManager.withFileLock("admins", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("admins");

            List<Admin> admins = new ArrayList<>();

            for (String record : records) {
                admins.add(convertToAdmin(record));
            }

            return admins;
        });
    }

    @Override
    public boolean update(String id, Admin updatedAdmin)
            throws FileStorageException {

        return storageLockManager.withFileLock("admins", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("admins");

            boolean found = false;

            for (int i = 0; i < records.size(); i++) {

                Admin admin = convertToAdmin(records.get(i));

                if (admin.getAdminId().equals(id)) {

                    records.set(i, convertToRecord(updatedAdmin));
                    found = true;
                    break;
                }
            }

            if (found) {
                txtFileStore.writeRecords("admins", records);
            }

            return found;
        });
    }

    @Override
    public boolean deleteById(String id)
            throws FileStorageException {

        return storageLockManager.withFileLock("admins", () -> {

            ArrayList<String> records =
                    txtFileStore.readRecords("admins");

            boolean removed = false;

            for (int i = 0; i < records.size(); i++) {

                Admin admin = convertToAdmin(records.get(i));

                if (admin.getAdminId().equals(id)) {

                    records.remove(i);
                    removed = true;
                    break;
                }
            }

            if (removed) {
                txtFileStore.writeRecords("admins", records);
            }

            return removed;
        });
    }

    private String convertToRecord(Admin admin)
            throws FileStorageException {

        List<String> fields = Arrays.asList(
                admin.getAdminId(),
                admin.getFullName(),
                admin.getEmail(),
                admin.getPasswordHash(),
                admin.getStatus().name(),
                admin.getCreatedAt(),
                admin.getUpdatedAt()
        );

        return TxtCodec.encodeRecord(fields);
    }

    private Admin convertToAdmin(String record)
            throws FileStorageException {

        List<String> fields = TxtCodec.decodeRecord(record, 7);

        return new Admin(
                fields.get(0),
                fields.get(1),
                fields.get(2),
                fields.get(3),
                AccountStatus.valueOf(fields.get(4)),
                fields.get(5),
                fields.get(6),
                fields.get(0)
        );
    }
}