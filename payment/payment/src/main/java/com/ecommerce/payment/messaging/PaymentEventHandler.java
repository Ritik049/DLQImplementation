//package com.ecommerce.payment.messaging;
//
//import com.ecommerce.payment.dto.OrderCreatedEvent;
//import com.ecommerce.payment.dto.PaymentCompletedEvent;
//import com.ecommerce.payment.dto.PaymentResponseDTO;
//import com.ecommerce.payment.service.PaymentService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.cloud.stream.function.StreamBridge;
//import org.springframework.context.annotation.Bean;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.function.Consumer;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class PaymentEventHandler {
//
//    private final PaymentService paymentService;
//    private final StreamBridge streamBridge;
//
//    // Name "processOrder" must match the binding name processOrder-in-0 in application.yml
//    @Bean
//    public Consumer<OrderCreatedEvent> processOrder() {
//        return event -> {
//            // Synchronous payment processing inside consumer thread:
//            PaymentResponseDTO resp = paymentService.processPaymentForOrderEvent(event);
//
//            //Error handling
//
//
//            // Build completed event and publish it to payment-topic
//            PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(
//                    event.getOrderId(),
//                    resp.getStatus(),
//                    resp.getTransactionId(),
//                    resp.getTransactionId() == null ? null : resp != null ? Double.valueOf(event.getTotalAmount().doubleValue()) : null,
//                    LocalDateTime.now(),
//                    resp.getStatus().equals("SUCCESS") ? null : resp.getMessage()
//            );
//
//            // send using the outbound binding name: publishPayment-out-0
//          // streamBridge.send("publishPayment-out-0", completedEvent);
//            log.info("Payment Service consumed order Event");
//        };
//    }
//}
//


package com.ecommerce.payment.messaging;

import com.ecommerce.payment.dto.OrderCreatedEvent;
import com.ecommerce.payment.dto.PaymentCompletedEvent;
import com.ecommerce.payment.dto.PaymentResponseDTO;
import com.ecommerce.payment.service.PaymentService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventHandler {

    private final PaymentService paymentService;
    private final StreamBridge streamBridge;

    private final MeterRegistry meterRegistry;

    private Counter paymentFailedCounter;

    @PostConstruct
    public void init() {
        paymentFailedCounter = Counter.builder("payment_failed_total")
                .description("Total number of failed payments")
                .register(meterRegistry);
    }

    /**
     * Consumes "order.created" events from Kafka (order.exchange).
     * Processes payment synchronously.
     * On success → publishes "payment.completed".
     * On failure → throws exception → routed to payment.dlq by binder.
     */
//    @Bean
//    public Consumer<OrderCreatedEvent> processOrder() {
//        return event -> {
//            log.info("Received OrderCreatedEvent: {}", event);
//            // Step 1: Process Payment
//            PaymentResponseDTO resp = paymentService.processPaymentForOrderEvent(event);
//
//            try {
//
//
//                // Step 2: If payment failed → throw exception (triggers DLQ)
//                if (!"SUCCESS".equalsIgnoreCase(resp.getStatus())) {
//                    log.error("Payment failed for orderId={} reason={}", event.getOrderId(), resp.getMessage());
//                    throw new RuntimeException("Payment failed: " + resp.getMessage());
//                }
//
//                // Step 3: Build PaymentCompletedEvent
//                PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(
//                        event.getOrderId(),
//                        resp.getStatus(),
//                        resp.getTransactionId(),
//                        event.getTotalAmount(),
//                        LocalDateTime.now(),
//                        null // no error message on success
//                );
//
//                // Step 4: Publish to payment.exchange
//                streamBridge.send("publishPayment-out-0", completedEvent);
//                log.info("Published PaymentCompletedEvent for orderId={}", event.getOrderId());
//
//            } catch (Exception ex) {
//                // Step 3: Build PaymentCompletedEvent
//                PaymentCompletedEvent failedEvent = new PaymentCompletedEvent(
//                        event.getOrderId(),
//                        "FAILED",
//                        resp.getTransactionId(),
//                        event.getTotalAmount(),
//                        LocalDateTime.now(),
//                        "Error processing in payment"
//                );
//
//                streamBridge.send("publishPayment-out-0", failedEvent);
//                log.error("Error processing payment for orderId={}", event.getOrderId(), ex);
//                throw ex; // important — lets Kafka binder send to DLQ
//            }
//        };
//    }

    @Bean
    public Consumer<OrderCreatedEvent> processOrder() {
        return event -> {
            log.info("Received OrderCreatedEvent: {}", event);
            PaymentResponseDTO resp = paymentService.processPaymentForOrderEvent(event);

            if (!"SUCCESS".equalsIgnoreCase(resp.getStatus())) {
                log.error("Payment failed for orderId={} reason={}", event.getOrderId(), resp.getMessage());

//                // Optional: Send failure notification event (business topic)
//                PaymentCompletedEvent failedEvent = new PaymentCompletedEvent(
//                        event.getOrderId(),
//                        "FAILED",
//                        resp.getTransactionId(),
//                        event.getTotalAmount(),
//                        LocalDateTime.now(),
//                        resp.getMessage()
//                );
//                streamBridge.send("publishPayment-out-0", failedEvent);
//
//                // Important: Throw to let Kafka binder route to DLQ
//                throw new RuntimeException("Payment failed: " + resp.getMessage());

                // Increment Prometheus metric
                paymentFailedCounter.increment();

                // Send alert to Kafka topic
                Map<String, Object> alert = Map.of(
                        "orderId", event.getOrderId(),
                        "status", "FAILED",
                        "reason", resp.getMessage(),
                        "timestamp", LocalDateTime.now().toString()
                );
                streamBridge.send("paymentAlerts-out-0", alert);

                // Throw exception → routed to DLQ
                throw new RuntimeException("Payment failed: " + resp.getMessage());
            }

            // If success
            PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(
                    event.getOrderId(),
                    resp.getStatus(),
                    resp.getTransactionId(),
                    event.getTotalAmount(),
                    LocalDateTime.now(),
                    null
            );
            streamBridge.send("publishPayment-out-0", completedEvent);
            log.info("Published PaymentCompletedEvent for orderId={}", event.getOrderId());
        };
    }

}
