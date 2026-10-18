
package com.example.studentregistration.shared.filehandling;

import com.example.studentregistration.shared.exception.FileStorageException;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class StorageLockManager {

    private final ConcurrentHashMap<String, ReentrantLock> locks =
            new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface FileOperation<T> {
        T execute() throws FileStorageException;
    }

    public <T> T withFileLock(
            String fileStem,
            FileOperation<T> operation
    ) throws FileStorageException {

        if (fileStem == null ||
                !fileStem.matches("[a-z][a-z0-9_]*")) {

            throw new FileStorageException(
                    "Invalid TXT filename");
        }

        if (operation == null) {
            throw new FileStorageException(
                    "File operation cannot be null");
        }

        ReentrantLock lock = locks.computeIfAbsent(
                fileStem,
                key -> new ReentrantLock()
        );

        lock.lock();

        try {
            return operation.execute();

        } finally {
            lock.unlock();
        }
    }
}
