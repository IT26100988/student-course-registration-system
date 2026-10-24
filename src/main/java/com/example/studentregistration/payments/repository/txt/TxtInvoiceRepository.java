package com.example.studentregistration.payments.repository.txt;

import com.example.studentregistration.payments.model.*;
import com.example.studentregistration.payments.repository.InvoiceRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class TxtInvoiceRepository implements InvoiceRepository {

    private final String FILE_PATH = "data/invoices.txt";

    public TxtInvoiceRepository() {

        File file = new File(FILE_PATH);
        file.getParentFile().mkdirs();
    }

    @Override
    public List<Invoice> findAll() {
        List<Invoice> invoices = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return invoices;

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = br.readLine()) != null) {

                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                if (line.trim().isEmpty()) continue;


                String[] parts = line.split("\\|", -1);
                if (parts.length >= 11) {
                    Invoice inv = new Invoice();
                    inv.setInvoiceId(parts[0]);
                    inv.setEnrollmentId(parts[1]);
                    inv.setStudentId(parts[2]);
                    inv.setOfferingId(parts[3]);
                    inv.setCurrency(parts[4]);


                    InvoiceLine lineItem = new InvoiceLine(parts[5], parts[6], new BigDecimal(parts[7]));
                    inv.setLine(lineItem);

                    inv.setTotalAmount(new BigDecimal(parts[8]));
                    inv.setStatus(InvoiceStatus.valueOf(parts[9]));
                    inv.setIssuedAt(Instant.parse(parts[10]));

                    invoices.add(inv);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading invoices: " + e.getMessage());
        }
        return invoices;
    }

    @Override
    public Invoice findById(String invoiceId) {
        return findAll().stream().filter(i -> i.getInvoiceId().equals(invoiceId)).findFirst().orElse(null);
    }

    @Override
    public void save(Invoice invoice) {
        List<Invoice> invoices = findAll();
        invoices.add(invoice);
        writeAll(invoices);
    }

    @Override
    public void update(Invoice invoice) {
        List<Invoice> invoices = findAll();
        for (int i = 0; i < invoices.size(); i++) {
            if (invoices.get(i).getInvoiceId().equals(invoice.getInvoiceId())) {
                invoices.set(i, invoice);
                break;
            }
        }
        writeAll(invoices);
    }


    private void writeAll(List<Invoice> invoices) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8))) {
            bw.write("#SCHEMA|invoices|1\n"); 
            for (Invoice inv : invoices) {
                String line = String.join("|",
                        inv.getInvoiceId(),
                        inv.getEnrollmentId(),
                        inv.getStudentId(),
                        inv.getOfferingId(),
                        inv.getCurrency(),
                        inv.getLine().getLineId(),
                        inv.getLine().getDescription(),
                        inv.getLine().getAmount().toString(),
                        inv.getTotalAmount().toString(),
                        inv.getStatus().name(),
                        inv.getIssuedAt().toString()
                );
                bw.write(line);
                bw.newLine();
            }
        } catch (Exception e) {
            System.err.println("Error writing invoices: " + e.getMessage());
        }
    }
}
