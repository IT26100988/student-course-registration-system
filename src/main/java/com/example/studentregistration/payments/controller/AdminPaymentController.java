package com.example.studentregistration.payments.controller;

import com.example.studentregistration.payments.model.Payment;
import com.example.studentregistration.payments.service.PaymentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/payments")
public class AdminPaymentController {

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }


    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    @PatchMapping("/{paymentId}/verification")
    public String verifyPayment(@PathVariable String paymentId) {

        String mockAdminId = "ADM_123";

        try {
            paymentService.verifyManualPayment(paymentId, mockAdminId);
            return "Payment verified successfully.";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}