
package com.example.studentregistration.shared.filehandling;

import com.example.studentregistration.shared.exception.FileStorageException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class StorageLockManagerTest {

    @Test
    void protectsConcurrentWrites(@TempDir Path directory)
            throws Exception {

        StorageLockManager manager =
                new StorageLockManager();

        // Two separate file-store objects use the same folder.
        TxtFileStore storeA =
                new TxtFileStore(directory.toString());

        TxtFileStore storeB =
                new TxtFileStore(directory.toString());

        String fileName = "lesson08_notes";

        ExecutorService pool =
                Executors.newFixedThreadPool(4);

        CountDownLatch startGate =
                new CountDownLatch(1);

        ArrayList<Future<?>> tasks =
                new ArrayList<>();

        try {
            // Simulate 12 simultaneous create requests.
            for (int i = 1; i <= 12; i++) {

                final int noteNumber = i;

                Future<?> task = pool.submit(() -> {

                    startGate.await();

                    TxtFileStore store =
                            (noteNumber % 2 == 0)
                                    ? storeA : storeB;

                    manager.withFileLock(fileName, () -> {

                        ArrayList<String> records =
                                store.readRecords(fileName);

                        String record =
                                TxtCodec.encodeRecord(
                                        List.of(
                                                "N" + noteNumber,
                                                "Note " + noteNumber
                                        )
                                );

                        records.add(record);

                        store.writeRecords(fileName, records);

                        return null;
                    });

                    return null;
                });

                tasks.add(task);
            }

            // Allow the queued tasks to start.
            startGate.countDown();

            for (Future<?> task : tasks) {
                task.get(15, TimeUnit.SECONDS);
            }

        } finally {
            pool.shutdownNow();
        }

        // Open the saved file using a third store object.
        TxtFileStore verifier =
                new TxtFileStore(directory.toString());

        ArrayList<String> saved =
                verifier.readRecords(fileName);

        assertEquals(12, saved.size());

        HashSet<String> uniqueRecords =
                new HashSet<>(saved);

        assertEquals(12, uniqueRecords.size());

        System.out.println(
                "ALL 12 RECORDS SAVED SUCCESSFULLY");

        System.out.println(
                "FINAL RECORD COUNT: " + saved.size());
    }

    @Test
    void rejectsInvalidFileNames() {

        StorageLockManager manager =
                new StorageLockManager();

        assertThrows(
                FileStorageException.class,
                () -> manager.withFileLock(
                        "../outside",
                        () -> "Invalid"
                )
        );

        assertThrows(
                FileStorageException.class,
                () -> manager.withFileLock(
                        "students",
                        null
                )
        );

        System.out.println(
                "INVALID LOCK REQUESTS REJECTED");
    }

    @Test
    void releasesLockAfterException()
            throws FileStorageException {

        StorageLockManager manager =
                new StorageLockManager();

        assertThrows(
                FileStorageException.class,
                () -> manager.withFileLock(
                        "lesson08_notes",
                        () -> {
                            throw new FileStorageException(
                                    "Simulated failure");
                        }
                )
        );

        String result = manager.withFileLock(
                "lesson08_notes",
                () -> "LOCK RELEASED"
        );

        assertEquals("LOCK RELEASED", result);

        System.out.println(
                "LOCK RELEASE TEST PASSED");
    }
}
