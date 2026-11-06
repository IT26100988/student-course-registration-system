package com.example.studentregistration.payments.controller;

import com.example.studentregistration.payments.model.Invoice;
import com.example.studentregistration.payments.model.Payment;
import com.example.studentregistration.payments.service.InvoiceService;
import com.example.studentregistration.payments.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students/me")
public class StudentPaymentController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;

    public StudentPaymentController(InvoiceService invoiceService, PaymentService paymentService) {
        this.invoiceService = invoiceService;
        this.paymentService = paymentService;
    }


    @GetMapping("/invoices")
    public List<Invoice> getMyInvoices() {

        return invoiceService.getAllInvoices();
    }


    @PostMapping("/payments/demo")
    public Payment makeDemoPayment(@RequestBody DemoPaymentRequest request) {

        String mockStudentId = "STU_123";
        return paymentService.processDemoPayment(request.getInvoiceId(), mockStudentId, request.getIdempotencyKey());
    }


    public static class DemoPaymentRequest {
        private String invoiceId;
        private String idempotencyKey;

        public String getInvoiceId() { return invoiceId; }
        public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

        public String getIdempotencyKey() { return idempotencyKey; }
        public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    }
}