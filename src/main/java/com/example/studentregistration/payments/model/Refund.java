package com.example.studentregistration.payments.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Refund {
    private String refundId;
    private String paymentId;
    private String studentId;
    private BigDecimal amount;
    private String currency;
    private String reason;
    private RefundStatus status;
    private Instant requestedAt;
    private Instant decidedAt;
    private String decidedByAdminId;
    private Instant completedAt;
    private String decisionNote;

    public Refund() {
    }

    public Refund(String refundId, String paymentId, String studentId, BigDecimal amount, String currency, String reason, RefundStatus status, Instant requestedAt) {
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.studentId = studentId;
        this.amount = amount;
        this.currency = currency;
        this.reason = reason;
        this.status = status;
        this.requestedAt = requestedAt;
    }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public RefundStatus getStatus() { return status; }
    public void setStatus(RefundStatus status) { this.status = status; }

    public Instant getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Instant requestedAt) { this.requestedAt = requestedAt; }

    public Instant getDecidedAt() { return decidedAt; }
    public void setDecidedAt(Instant decidedAt) { this.decidedAt = decidedAt; }

    public String getDecidedByAdminId() { return decidedByAdminId; }
    public void setDecidedByAdminId(String decidedByAdminId) { this.decidedByAdminId = decidedByAdminId; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public String getDecisionNote() { return decisionNote; }
    public void setDecisionNote(String decisionNote) { this.decisionNote = decisionNote; }
}
