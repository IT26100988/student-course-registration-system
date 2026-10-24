package com.example.studentregistration.payments.repository;

import com.example.studentregistration.payments.model.Payment;
import java.util.List;

public interface PaymentRepository {
    List<Payment> findAll();
    Payment findById(String paymentId);
    Payment findByIdempotencyKey(String key);
    void save(Payment payment);
    void update(Payment payment);
}