package com.ecommerce.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
    private Long orderId;
    private String status; // SUCCESS/FAILED
    private String transactionId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private String failureReason;

    public PaymentCompletedEvent(long l, String status) {
        this.orderId = l;
        this.status = status;
    }
}
