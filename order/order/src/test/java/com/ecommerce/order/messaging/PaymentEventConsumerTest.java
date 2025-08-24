package com.ecommerce.order.messaging;

import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.dto.PaymentCompletedEvent;
import com.ecommerce.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentEventConsumerTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private StreamBridge streamBridge;

    @Autowired
    private PaymentEventConsumer consumer;

    @Test
    void shouldUpdateOrderStatusOnSuccess() {
        // Arrange
        PaymentCompletedEvent event = new PaymentCompletedEvent(1L, "SUCCESS");
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        // Act
        consumer.updateOrderStatus().accept(event);

        // Assert
        assertEquals(OrderStatus.CONFIRMED, order.getStatus()); // Match actual mapped status
        verify(orderRepository).save(order);
        verify(streamBridge).send(eq("updateOrderStatus-out-0"), any());
    }

    @Test
    void shouldEmitFailureEventIfOrderNotFound() {
        // Arrange
        PaymentCompletedEvent event = new PaymentCompletedEvent(999L, "SUCCESS");
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        consumer.updateOrderStatus().accept(event);

        // Assert
        verify(orderRepository, never()).save(any());
        verify(streamBridge).send(eq("updateOrderStatus-out-0"), eq("Order not processed correctly"));
    }
}