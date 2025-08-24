package com.ecommerce.payment.service;

import com.ecommerce.payment.dto.PaymentRequestDTO;
import com.ecommerce.payment.dto.PaymentResponseDTO;
import com.ecommerce.payment.model.Payment;
import com.ecommerce.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    // existing method you already had (no change)
    public PaymentResponseDTO processPayment(PaymentRequestDTO request) {
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}

        double random = Math.random();
        boolean gatewayAvailable = random > 0.1;
        boolean paymentSuccess = random > 0.3;

        String status;
        String failureReason = null;

        if (!gatewayAvailable) {
            status = "FAILED";
            failureReason = "Payment gateway unavailable";
        } else if (!paymentSuccess) {
            status = "FAILED";
            failureReason = "Payment declined by bank";
        } else {
            status = "SUCCESS";
        }

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(status)
                .failureReason(failureReason)
                .transactionId(UUID.randomUUID().toString())
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        System.out.println("Payment Processiong  "+request.getOrderId());

        return PaymentResponseDTO.builder()
                .orderId(request.getOrderId())
                .status(status)
                .message(status.equals("SUCCESS") ? "Payment processed successfully" : "Payment failed: " + failureReason)
                .transactionId(payment.getTransactionId())
                .build();
    }

    // adapter method for event-based flow
    public PaymentResponseDTO processPaymentForOrderEvent(com.ecommerce.payment.dto.OrderCreatedEvent event) {
        PaymentRequestDTO req = PaymentRequestDTO.builder()
                .orderId(String.valueOf(event.getOrderId()))
                .amount(event.getTotalAmount().doubleValue())
                .paymentMethod("CARD") // default; adjust as you'd like or pick from event
                .build();
        return processPayment(req);
    }
}
