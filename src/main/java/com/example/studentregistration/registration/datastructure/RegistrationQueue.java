
package com.example.studentregistration.registration.datastructure;

import com.example.studentregistration.registration.model.RegistrationRequest;
import com.example.studentregistration.registration.model.RequestStatus;
import com.example.studentregistration.registration.repository.RegistrationRequestRepository;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Queue;

@Component
public class RegistrationQueue {

    private final RegistrationRequestRepository repository;

    private final Queue<RegistrationRequest> queue =
            new ArrayDeque<>();

    public RegistrationQueue(RegistrationRequestRepository repository) {
        this.repository = Objects.requireNonNull(repository);
        rebuild();
    }

    public synchronized void rebuild() {

        ArrayList<RegistrationRequest> pending =
                new ArrayList<>();

        for (RegistrationRequest request : repository.findAll()) {

            if (request.getStatus() == RequestStatus.PENDING) {
                pending.add(request);
            }
        }

        pending.sort(Comparator.comparingLong(
                RegistrationRequest::getSequenceNo));

        Queue<RegistrationRequest> rebuilt =
                new ArrayDeque<>();

        long previousSequence = 0;

        for (RegistrationRequest request : pending) {

            if (request.getSequenceNo() <= previousSequence) {
                throw new IllegalStateException(
                        "Duplicate or invalid FIFO sequence number");
            }

            rebuilt.offer(request);
            previousSequence = request.getSequenceNo();
        }

        queue.clear();
        queue.addAll(rebuilt);
    }

    public synchronized void enqueue(RegistrationRequest request) {

        Objects.requireNonNull(request, "Request is required");

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Only PENDING requests can be enqueued");
        }

        RegistrationRequest saved = repository
                .findById(request.getRequestId())
                .orElseThrow(() -> new IllegalStateException(
                        "Save the request before enqueueing it"));

        if (saved.getStatus() != RequestStatus.PENDING
                || saved.getSequenceNo() != request.getSequenceNo()) {
            throw new IllegalStateException(
                    "Saved request does not match pending request");
        }

        for (RegistrationRequest existing : queue) {

            if (existing.getRequestId().equals(saved.getRequestId())
                    || existing.getSequenceNo() >= saved.getSequenceNo()) {

                throw new IllegalStateException(
                        "Duplicate or out-of-order FIFO request");
            }
        }

        queue.offer(saved);
    }

    public synchronized RegistrationRequest peek() {
        return queue.peek();
    }

    public synchronized RegistrationRequest poll() {
        return queue.poll();
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}
