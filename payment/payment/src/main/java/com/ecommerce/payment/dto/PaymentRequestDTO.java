package com.ecommerce.payment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    private String orderId;
    private Double amount;
    private String paymentMethod;
}
