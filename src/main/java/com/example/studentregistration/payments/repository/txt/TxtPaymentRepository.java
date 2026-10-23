package com.example.studentregistration.payments.repository.txt;

import com.example.studentregistration.payments.model.*;
import com.example.studentregistration.payments.repository.PaymentRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TxtPaymentRepository implements PaymentRepository {

    private final String FILE_PATH = "data/payments.txt";

    public TxtPaymentRepository() {
        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
    }

    @Override
    public List<Payment> findAll() {
        List<Payment> payments = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return payments;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {
                if (isFirstLine) { isFirstLine = false; continue; }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split("\\|", -1);
                if (parts.length >= 11) {
                    Payment p = new Payment();
                    p.setPaymentId(parts[0]);
                    p.setInvoiceId(parts[1]);
                    p.setStudentId(parts[2]);
                    p.setMethod(PaymentMethod.valueOf(parts[3]));
                    p.setIdempotencyKey(parts[4]);
                    p.setExternalReference(parts[5].isEmpty() ? null : parts[5]);
                    p.setAmount(new BigDecimal(parts[6]));
                    p.setCurrency(parts[7]);
                    p.setStatus(PaymentStatus.valueOf(parts[8]));
                    p.setSubmittedAt(Instant.parse(parts[9]));
                    p.setVerifiedAt(parts[10].isEmpty() ? null : Instant.parse(parts[10]));

                    payments.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading payments: " + e.getMessage());
        }
        return payments;
    }

    @Override
    public Payment findById(String paymentId) {
        return findAll().stream().filter(p -> p.getPaymentId().equals(paymentId)).findFirst().orElse(null);
    }

    @Override
    public Payment findByIdempotencyKey(String key) {
        return findAll().stream().filter(p -> key.equals(p.getIdempotencyKey())).findFirst().orElse(null);
    }

    @Override
    public void save(Payment payment) {
        List<Payment> payments = findAll();
        payments.add(payment);
        writeAll(payments);
    }

    @Override
    public void update(Payment payment) {
        List<Payment> payments = findAll();
        for (int i = 0; i < payments.size(); i++) {
            if (payments.get(i).getPaymentId().equals(payment.getPaymentId())) {
                payments.set(i, payment);
                break;
            }
        }
        writeAll(payments);
    }

    private void writeAll(List<Payment> payments) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8))) {
            bw.write("#SCHEMA|payments|1\n");
            for (Payment p : payments) {
                String line = String.join("|",
                        p.getPaymentId(),
                        p.getInvoiceId(),
                        p.getStudentId(),
                        p.getMethod().name(),
                        p.getIdempotencyKey(),
                        p.getExternalReference() == null ? "" : p.getExternalReference(),
                        p.getAmount().toString(),
                        p.getCurrency(),
                        p.getStatus().name(),
                        p.getSubmittedAt().toString(),
                        p.getVerifiedAt() == null ? "" : p.getVerifiedAt().toString()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error writing payments: " + e.getMessage());
        }
    }
}