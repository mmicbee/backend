package com.zone01kisumu.backend.controller;

import com.zone01kisumu.backend.dto.PaymentInitiationRequest;
import com.zone01kisumu.backend.dto.PaymentResponse;
import com.zone01kisumu.backend.model.Payment;
import com.zone01kisumu.backend.service.PaystackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaystackService paystackService;

    @PostMapping("/initialize")
    public ResponseEntity<Object> initializePayment(@Valid @RequestBody PaymentInitiationRequest request) {
        try {
            PaymentResponse response = paystackService.initializePayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/verify/{reference}")
    public ResponseEntity<Object> verifyPayment(@PathVariable String reference) {
        try {
            Payment payment = paystackService.verifyAndUpdatePayment(reference);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error verifying payment: " + e.getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload,
            @RequestHeader("x-paystack-signature") String signature) {
        // Handle Paystack webhook for real-time payment updates
        try {
            // Verify webhook signature here (implement signature verification)
            // Parse payload and update payment status
            paystackService.handleWebhook(payload, signature);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Webhook processing failed" + e.getMessage());
        }
    }

    @GetMapping("/{reference}")
    public ResponseEntity<Payment> getPayment(@PathVariable String reference) {
        try {
            Payment payment = paystackService.getPaymentByReference(reference);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
