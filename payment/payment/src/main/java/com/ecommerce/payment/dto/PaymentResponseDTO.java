package com.ecommerce.payment.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private String orderId;
    private String status;
    private String message;
    private String transactionId;
}
