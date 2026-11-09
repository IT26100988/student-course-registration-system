package com.example.studentregistration.payments.exception;

public class InvoiceNotPayableException extends RuntimeException {
    public InvoiceNotPayableException(String message) {
        super(message);
    }
}
