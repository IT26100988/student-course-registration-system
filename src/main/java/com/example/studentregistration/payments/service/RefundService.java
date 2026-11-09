package com.example.studentregistration.payments.service;

import com.example.studentregistration.payments.model.*;
import com.example.studentregistration.payments.repository.RefundRepository;
import com.example.studentregistration.payments.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    public RefundService(RefundRepository refundRepository, PaymentRepository paymentRepository) {
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
    }

    public Refund requestRefund(String paymentId, String studentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId);

        if (payment == null || payment.getStatus() != PaymentStatus.VERIFIED) {
            throw new RuntimeException("Only verified payments can be refunded.");
        }

        Refund refund = new Refund();
        refund.setRefundId("RFD_" + UUID.randomUUID().toString());
        refund.setPaymentId(paymentId);
        refund.setStudentId(studentId);
        refund.setAmount(payment.getAmount());
        refund.setCurrency(payment.getCurrency());
        refund.setReason(reason);
        refund.setStatus(RefundStatus.REQUESTED);
        refund.setRequestedAt(Instant.now());

        refundRepository.save(refund);
        return refund;
    }

    public List<Refund> getAllRefunds() {
        return refundRepository.findAll();
    }


    public void approveRefund(String refundId, String adminId) {
        Refund refund = refundRepository.findById(refundId);
        if (refund != null && refund.getStatus() == RefundStatus.REQUESTED) {
            refund.setStatus(RefundStatus.APPROVED);
            refund.setDecidedAt(Instant.now());
            refund.setDecidedByAdminId(adminId);
            refundRepository.update(refund);
        }
    }

    public void completeRefund(String refundId) {
        Refund refund = refundRepository.findById(refundId);
        if (refund != null && refund.getStatus() == RefundStatus.APPROVED) {
            refund.setStatus(RefundStatus.COMPLETED);
            refund.setCompletedAt(Instant.now());
            refundRepository.update(refund);


            Payment payment = paymentRepository.findById(refund.getPaymentId());
            if (payment != null) {

            }
        }
    }
}