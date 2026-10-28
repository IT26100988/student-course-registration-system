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


    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice createInvoice(String enrollmentId, String studentId, String offeringId, BigDecimal amount) {
        Invoice invoice = new Invoice();

        invoice.setInvoiceId("INV_" + UUID.randomUUID().toString());
        invoice.setEnrollmentId(enrollmentId);
        invoice.setStudentId(studentId);
        invoice.setOfferingId(offeringId);
        invoice.setCurrency("LKR");
        invoice.setTotalAmount(amount);
        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setIssuedAt(Instant.now());


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

    public void markAsPaid(String invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId);
        if (invoice != null) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setPaidAt(Instant.now());
            invoiceRepository.update(invoice);
        }
    }
}