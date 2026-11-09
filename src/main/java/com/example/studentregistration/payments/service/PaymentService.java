package com.example.studentregistration.payments.service;

import com.example.studentregistration.payments.exception.InvoiceNotPayableException;
import com.example.studentregistration.payments.model.*;
import com.example.studentregistration.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceService invoiceService;


    public PaymentService(PaymentRepository paymentRepository, InvoiceService invoiceService) {
        this.paymentRepository = paymentRepository;
        this.invoiceService = invoiceService;
    }


    public Payment processDemoPayment(String invoiceId, String studentId, String idempotencyKey) {
        Invoice invoice = invoiceService.getInvoiceById(invoiceId);


        if (invoice == null) {
            throw new InvoiceNotPayableException("Invoice not found.");
        }
        if (invoice.getStatus() != InvoiceStatus.ISSUED) {
            throw new InvoiceNotPayableException("This invoice is already paid or voided.");
        }

        Payment existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existingPayment != null) {
            return existingPayment;
        }

        Payment payment = new Payment();
        payment.setPaymentId("PAY_" + UUID.randomUUID().toString());
        payment.setInvoiceId(invoiceId);
        payment.setStudentId(studentId);
        payment.setMethod(PaymentMethod.DEMO);
        payment.setIdempotencyKey(idempotencyKey);
        payment.setAmount(invoice.getTotalAmount());
        payment.setCurrency(invoice.getCurrency());
        payment.setStatus(PaymentStatus.VERIFIED);
        payment.setSubmittedAt(Instant.now());
        payment.setVerifiedAt(Instant.now());

        paymentRepository.save(payment);


        invoiceService.markAsPaid(invoiceId);

        return payment;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }


    public void verifyManualPayment(String paymentId, String adminId) {
        Payment payment = paymentRepository.findById(paymentId);
        if (payment != null && payment.getStatus() == PaymentStatus.PENDING_VERIFICATION) {
            payment.setStatus(PaymentStatus.VERIFIED);
            payment.setVerifiedAt(Instant.now());
            payment.setVerifiedByAdminId(adminId);
            paymentRepository.update(payment);

            invoiceService.markAsPaid(payment.getInvoiceId());
        }
    }
}
