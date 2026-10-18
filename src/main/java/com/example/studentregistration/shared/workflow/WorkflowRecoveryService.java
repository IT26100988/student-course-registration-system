
package com.example.studentregistration.shared.workflow;

import com.example.studentregistration.shared.exception.FileStorageException;
import com.example.studentregistration.shared.filehandling.StorageLockManager;
import com.example.studentregistration.shared.filehandling.TxtCodec;
import com.example.studentregistration.shared.filehandling.TxtFileStore;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class WorkflowRecoveryService {

    private static final String FILE_NAME = "workflow_journal";

    private static final String UUID_PATTERN =
            "[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}";

    private static final Set<String> OPERATIONS = Set.of(
            "APPROVE_ENROLL",
            "VERIFY_PAY",
            "EXPIRE_ENROLL",
            "APPLY_GRADE_REVIEW",
            "COMPLETE_REFUND"
    );

    private final TxtFileStore fileStore;
    private final StorageLockManager lockManager;

    public WorkflowRecoveryService(
            TxtFileStore fileStore,
            StorageLockManager lockManager) {

        this.fileStore = fileStore;
        this.lockManager = lockManager;
    }

    public record IncompleteTransaction(
            String txnId,
            String operation,
            String primaryId,
            String lastStage,
            int eventCount) {
    }

    private record JournalEvent(
            String txnId,
            String operation,
            String primaryId,
            String stage,
            Instant recordedAt) {
    }

    public ArrayList<IncompleteTransaction>
    findIncompleteTransactions() throws FileStorageException {

        return lockManager.withFileLock(FILE_NAME, () -> {

            ArrayList<String> records =
                    fileStore.readRecords(FILE_NAME);

            // NEW IN STEP 12:
            // Reject incomplete journal endings before processing.
            fileStore.requireTrailingNewline(FILE_NAME);

            LinkedHashMap<String, ArrayList<JournalEvent>> grouped =
                    new LinkedHashMap<>();

            for (String record : records) {

                JournalEvent event = decodeEvent(record);

                ArrayList<JournalEvent> history =
                        grouped.computeIfAbsent(
                                event.txnId(),
                                key -> new ArrayList<>()
                        );

                if (history.isEmpty()) {

                    if (!event.stage().equals("BEGIN")) {
                        throw new FileStorageException(
                                "Transaction must begin with BEGIN");
                    }

                } else {

                    JournalEvent previous =
                            history.get(history.size() - 1);

                    if (!previous.operation()
                            .equals(event.operation())
                            || !previous.primaryId()
                            .equals(event.primaryId())) {

                        throw new FileStorageException(
                                "Inconsistent journal transaction");
                    }

                    if (previous.stage().equals("DONE")
                            || event.stage().equals("BEGIN")) {

                        throw new FileStorageException(
                                "Invalid journal stage order");
                    }

                    if (event.recordedAt().isBefore(
                            previous.recordedAt())) {

                        throw new FileStorageException(
                                "Journal timestamps are out of order");
                    }
                }

                history.add(event);
            }

            ArrayList<IncompleteTransaction> incomplete =
                    new ArrayList<>();

            for (Map.Entry<String, ArrayList<JournalEvent>> group
                    : grouped.entrySet()) {

                ArrayList<JournalEvent> history =
                        group.getValue();

                JournalEvent last =
                        history.get(history.size() - 1);

                if (!last.stage().equals("DONE")) {

                    incomplete.add(new IncompleteTransaction(
                            last.txnId(),
                            last.operation(),
                            last.primaryId(),
                            last.stage(),
                            history.size()
                    ));
                }
            }

            return incomplete;
        });
    }

    private JournalEvent decodeEvent(String record)
            throws FileStorageException {

        ArrayList<String> fields =
                TxtCodec.decodeRecord(record, 6);

        String txnId = fields.get(0);
        String operation = fields.get(1);
        String primaryId = fields.get(2);
        String stage = fields.get(3);
        String payload = fields.get(4);
        String timestamp = fields.get(5);

        if (txnId == null
                || !txnId.matches("TXN_" + UUID_PATTERN)
                || operation == null
                || !OPERATIONS.contains(operation)
                || primaryId == null
                || !primaryId.matches(
                "[A-Z]{3}_" + UUID_PATTERN)
                || stage == null
                || !stage.matches("[A-Z][A-Z0-9_]*")
                || payload == null
                || payload.isBlank()
                || !payload.contains("=")
                || timestamp == null) {

            throw new FileStorageException(
                    "Invalid workflow journal record");
        }

        try {

            Instant recordedAt = Instant.parse(timestamp);

            if (!recordedAt.toString().equals(timestamp)) {
                throw new FileStorageException(
                        "Non-canonical journal timestamp");
            }

            return new JournalEvent(
                    txnId,
                    operation,
                    primaryId,
                    stage,
                    recordedAt
            );

        } catch (DateTimeParseException e) {

            throw new FileStorageException(
                    "Invalid workflow journal timestamp", e);
        }
    }
}
