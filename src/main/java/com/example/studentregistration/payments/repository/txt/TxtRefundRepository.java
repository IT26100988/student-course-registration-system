package com.example.studentregistration.payments.repository.txt;

import com.example.studentregistration.payments.model.*;
import com.example.studentregistration.payments.repository.RefundRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TxtRefundRepository implements RefundRepository {

    private final String FILE_PATH = "data/refunds.txt";

    public TxtRefundRepository() {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
    }

    @Override
    public List<Refund> findAll() {
        List<Refund> refunds = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return refunds;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 8) {
                    Refund r = new Refund();
                    r.setRefundId(parts[0]);
                    r.setPaymentId(parts[1]);
                    r.setStudentId(parts[2]);
                    r.setAmount(new BigDecimal(parts[3]));
                    r.setCurrency(parts[4]);
                    r.setReason(parts[5]);
                    r.setStatus(RefundStatus.valueOf(parts[6]));
                    r.setRequestedAt(Instant.parse(parts[7]));

                    refunds.add(r);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading refunds: " + e.getMessage());
        }
        return refunds;
    }

    @Override
    public Refund findById(String refundId) {
        return findAll().stream().filter(r -> r.getRefundId().equals(refundId)).findFirst().orElse(null);
    }

    @Override
    public void save(Refund refund) {
        List<Refund> refunds = findAll();
        refunds.add(refund);
        writeAll(refunds);
    }

    @Override
    public void update(Refund refund) {
        List<Refund> refunds = findAll();
        for (int i = 0; i < refunds.size(); i++) {
            if (refunds.get(i).getRefundId().equals(refund.getRefundId())) {
                refunds.set(i, refund);
                break;
            }
        }
        writeAll(refunds);
    }

    private void writeAll(List<Refund> refunds) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8))) {
            bw.write("#SCHEMA|refunds|1\n");
            for (Refund r : refunds) {
                String line = String.join("|",
                        r.getRefundId(),
                        r.getPaymentId(),
                        r.getStudentId(),
                        r.getAmount().toString(),
                        r.getCurrency(),
                        r.getReason(),
                        r.getStatus().name(),
                        r.getRequestedAt().toString()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error writing refunds: " + e.getMessage());
        }
    }
}