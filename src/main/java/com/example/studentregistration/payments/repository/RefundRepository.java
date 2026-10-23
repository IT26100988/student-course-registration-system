package com.example.studentregistration.payments.repository;

import com.example.studentregistration.payments.model.Refund;
import java.util.List;

public interface RefundRepository {
    List<Refund> findAll();
    Refund findById(String refundId);
    void save(Refund refund);
    void update(Refund refund);
}