package com.ecommerce.payment.controller;


import com.ecommerce.payment.dto.PaymentRequestDTO;
import com.ecommerce.payment.dto.PaymentResponseDTO;
import com.ecommerce.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponseDTO> makePayment(@RequestBody PaymentRequestDTO request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }
}