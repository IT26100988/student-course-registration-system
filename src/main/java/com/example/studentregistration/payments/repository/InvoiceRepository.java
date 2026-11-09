package com.example.studentregistration.payments.repository;

import com.example.studentregistration.payments.model.Invoice;
import java.util.List;

public interface InvoiceRepository {
    List<Invoice> findAll();
    Invoice findById(String invoiceId);
    void save(Invoice invoice);
    void update(Invoice invoice);
}