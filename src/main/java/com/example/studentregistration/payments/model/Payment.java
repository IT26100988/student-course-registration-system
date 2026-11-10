package com.example.studentregistration.payments.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Payment {
    private String paymentId;
    private String invoiceId;
    private String studentId;
    private PaymentMethod method; // Enum (DEMO හෝ MANUAL)
    private String idempotencyKey;
    private String externalReference;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status; // Enum
    private Instant submittedAt;
    private Instant verifiedAt;
    private String verifiedByAdminId;
    private String failureReason;

    public Payment() {
    }

    public Payment(String paymentId, String invoiceId, String studentId, PaymentMethod method, String idempotencyKey, BigDecimal amount, String currency, PaymentStatus status, Instant submittedAt) {
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.studentId = studentId;
        this.method = method;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.submittedAt = submittedAt;
    }


    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public PaymentMethod getMethod() { return method; }
    public void setMethod(PaymentMethod method) { this.method = method; }

    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public String getExternalReference() { return externalReference; }
    public void setExternalReference(String externalReference) { this.externalReference = externalReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getVerifiedByAdminId() { return verifiedByAdminId; }
    public void setVerifiedByAdminId(String verifiedByAdminId) { this.verifiedByAdminId = verifiedByAdminId; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}