package com.example.studentregistration.payments.model;

import java.math.BigDecimal;

public class InvoiceLine {
    private String lineId;
    private String description;
    private BigDecimal amount;

    public InvoiceLine() {
    }

    public InvoiceLine(String lineId, String description, BigDecimal amount) {
        this.lineId = lineId;
        this.description = description;
        this.amount = amount;
    }

    public String getLineId() { return lineId; }
    public void setLineId(String lineId) { this.lineId = lineId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
