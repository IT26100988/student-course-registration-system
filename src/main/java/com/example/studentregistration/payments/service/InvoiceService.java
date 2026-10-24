package com.example.studentregistration.payments.service;

import com.example.studentregistration.payments.model.*;
import com.example.studentregistration.payments.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;

    // Constructor Injection (Spring Boot මගින් automatically හරියටම repository එක inject කරයි)
    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // අලුත් Invoice එකක් හදන method එක
    public Invoice createInvoice(String enrollmentId, String studentId, String offeringId, BigDecimal amount) {
        Invoice invoice = new Invoice();
        // UUID පාවිච්චි කරලා අද්විතීය (unique) ID එකක් හදනවා
        invoice.setInvoiceId("INV_" + UUID.randomUUID().toString());
        invoice.setEnrollmentId(enrollmentId);
        invoice.setStudentId(studentId);
        invoice.setOfferingId(offeringId);
        invoice.setCurrency("LKR");
        invoice.setTotalAmount(amount);
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedAt(Instant.now());

        // Composition: InvoiceLine එක හදලා Invoice එකට අමුණනවා
        InvoiceLine line = new InvoiceLine(
                "LIN_" + UUID.randomUUID().toString(),
                "Course Fee",
                amount
        );
        invoice.setLine(line);

        invoiceRepository.save(invoice);
        return invoice;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getInvoiceById(String id) {
        return invoiceRepository.findById(id);
    }

    // ගෙවීමක් සාර්ථක වුනාම invoice එකේ තත්වය (status) PAID කරන්න
    public void markAsPaid(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId);
        if (invoice != null) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(Instant.now());
            invoiceRepository.update(invoice);
        }
    }
}