package com.example.studentregistration.payments.controller;

import com.example.studentregistration.payments.model.Refund;
import com.example.studentregistration.payments.service.RefundService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }


    @PostMapping("/students/me/refunds")
    public Refund requestRefund(@RequestBody RefundRequestDto request) {
        String mockStudentId = "STU_123";
        return refundService.requestRefund(request.getPaymentId(), mockStudentId, request.getReason());
    }


    @GetMapping("/admin/refunds")
    public List<Refund> getAllRefunds() {
        return refundService.getAllRefunds();
    }


    @PatchMapping("/admin/refunds/{refundId}/decision")
    public String approveRefund(@PathVariable String refundId) {
        String mockAdminId = "ADM_123";
        refundService.approveRefund(refundId, mockAdminId);
        return "Refund approved.";
    }


    @PostMapping("/admin/refunds/{refundId}/complete")
    public String completeRefund(@PathVariable String refundId) {
        refundService.completeRefund(refundId);
        return "Refund completed.";
    }


    public static class RefundRequestDto {
        private String paymentId;
        private String reason;

        public String getPaymentId() { return paymentId; }
        public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}