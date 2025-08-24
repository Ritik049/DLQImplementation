package com.ecommerce.payment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;      // Comes from OrderCreatedEvent
    private Double amount;       // Payment amount
    private String status;       // SUCCESS / FAILED / PENDING
    private LocalDateTime paymentDate;

    private String failureReason;
    private String transactionId;

    private String paymentMethod; // CARD, UPI, NETBANKING
}
