package com.ecommerce.order.messaging;



import com.ecommerce.order.dto.OrderCreatedEvent;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.PaymentCompletedEvent;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderRepository orderRepository;
    private final StreamBridge streamBridge;

    @Bean
    public Consumer<PaymentCompletedEvent> updateOrderStatus() {
        return event -> {
            log.info("Received payment event for orderId={} with status={}",
                    event.getOrderId(), event.getStatus());

            Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());

            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                if ("SUCCESS".equalsIgnoreCase(event.getStatus())) {
                    order.setStatus(OrderStatus.CONFIRMED);
                } else {
                    order.setStatus(OrderStatus.PAYMENT_FAILED);
                }
              Order saved =   orderRepository.save(order);

                OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                        saved.getId(),
                        saved.getUserId(),
                        saved.getStatus(),
                        saved.getTotalAmount(),
                        saved.getItems().stream()
                                .map(i -> new OrderItemDTO(
                                        i.getId(),
                                        Long.valueOf(i.getProductId()),
                                        i.getQuantity(),
                                        i.getPrice(),
                                        i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))
                                ))
                                .collect(Collectors.toList()),
                        saved.getCreatedAt()
                );

                Message<OrderCreatedEvent>message = MessageBuilder.withPayload(orderCreatedEvent).build();
                streamBridge.send("updateOrderStatus-out-0", message);
                log.info("Order {} updated to status {}", order.getId(), order.getStatus());
            } else {

                streamBridge.send("updateOrderStatus-out-0","Order not processed correctly");
                log.warn("Order with ID {} not found!", event.getOrderId());
            }

            //Creating order event again


        };
    }
}
