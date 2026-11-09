package com.example.studentregistration.payments.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Invoice {
    private String invoiceId;
    private String enrollmentId;
    private String studentId;
    private String offeringId;
    private String currency;
    private BigDecimal totalAmount;
    private InvoiceStatus status; // Enum
    private Instant issuedAt;
    private Instant paidAt;
    private Instant voidedAt;


    private InvoiceLine line;

    public Invoice() {
    }

    public Invoice(String invoiceId, String enrollmentId, String studentId, String offeringId, String currency, BigDecimal totalAmount, InvoiceStatus status, Instant issuedAt, InvoiceLine line) {
        this.invoiceId = invoiceId;
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.offeringId = offeringId;
        this.currency = currency;
        this.totalAmount = totalAmount;
        this.status = status;
        this.issuedAt = issuedAt;
        this.line = line;
    }


    public String getInvoiceId() { return invoiceId; }
    public void setInvoiceId(String invoiceId) { this.invoiceId = invoiceId; }

    public String getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(String enrollmentId) { this.enrollmentId = enrollmentId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getOfferingId() { return offeringId; }
    public void setOfferingId(String offeringId) { this.offeringId = offeringId; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public Instant getIssuedAt() { return issuedAt; }
    public void setIssuedAt(Instant issuedAt) { this.issuedAt = issuedAt; }

    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }

    public Instant getVoidedAt() { return voidedAt; }
    public void setVoidedAt(Instant voidedAt) { this.voidedAt = voidedAt; }

    public InvoiceLine getLine() { return line; }
    public void setLine(InvoiceLine line) { this.line = line; }
}
