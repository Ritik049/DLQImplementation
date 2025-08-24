package com.ecommerce.order.dto;

import com.ecommerce.order.model.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor

public class OrderResponse {
    private Long id;
    private BigDecimal totalAmount;
    private OrderStatus orderStatus;
    private List<OrderItemDTO> items;

    private LocalDateTime createdAt;
//    private LocalDateTime updatedAt;

    public OrderResponse(Long id, BigDecimal totalAmount, OrderStatus status, List<OrderItemDTO> list, LocalDateTime createdAt) {
    this.id  = id;
    this.totalAmount = totalAmount;
    this.orderStatus = status;
    this.items = list;
    this.createdAt = createdAt;
    }


}
