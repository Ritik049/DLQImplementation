//package com.ecommerce.order.service;
//
//
//import com.ecommerce.order.clients.UserServiceClient;
//import com.ecommerce.order.dto.OrderCreatedEvent;
//import com.ecommerce.order.dto.OrderItemDTO;
//import com.ecommerce.order.dto.OrderResponse;
//import com.ecommerce.order.dto.UserResponse;
//import com.ecommerce.order.model.OrderItem;
//import com.ecommerce.order.model.OrderStatus;
//import com.ecommerce.order.model.CartItem;
//import com.ecommerce.order.model.Order;
//import com.ecommerce.order.repository.OrderRepository;
////import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.stream.function.StreamBridge;
//import org.springframework.stereotype.Service;
//
//import java.math.BigDecimal;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Service
//public class OrderService {
//
//    @Autowired
//    OrderRepository orderRepository;
//
//    @Autowired
//    CartService cartService;
//
////    @Autowired
////    RabbitTemplate rabbitTemplate;
//
////    @Value("${rabbitmq.exchange.name}")
////    private String exchangeName;
////
////    @Value("${rabbitmq.routing.key}")
////    private String routingKey;
//
//       @Autowired
//       private  StreamBridge streamBridge;
//
////    @Autowired
////    UserRepository userRepository;
//    @Autowired
//    UserServiceClient userServiceClient;
//
//    public Optional<OrderResponse> createOrder(String userId)
//    {
//        //Validate for cartItems
//        List<CartItem> cartItems = cartService.getCart(userId);
//
//        if(cartItems.isEmpty())
//        {
//            return Optional.empty();
//        }
//
//
//
//        //Validate for users
//
//        UserResponse userResponse = userServiceClient.getUserDetails(userId);
//        if(userResponse==null) return Optional.empty();
////        Optional<User> userOptional = userRepository.findById(Long.valueOf(userId));
////
////        if(userOptional.isEmpty())
////        {
////            return Optional.empty();
////
////        }
////        User user  = userOptional.get();
//
//        //Calcuate total Price
//        BigDecimal totalPrice = cartItems.stream().map(CartItem :: getPrice).reduce(BigDecimal.ZERO,BigDecimal::add);
//
//        //Create Order
//        Order order = new Order();
//        order.setUserId(userId);
//        order.setStatus(OrderStatus.CONFIRMED);
//        order.setTotalAmount(totalPrice);
//
//        List<OrderItem> orderItems = cartItems.stream().map(item->new OrderItem(null,item.getProductId(),item.getQuantity(),item.getPrice(),order)).collect(Collectors.toList());
//        order.setItems(orderItems);
//
//        Order savedOrder = orderRepository.save(order);
//
//        //Clear the cart
//        cartService.clearCart(userId);
//
//        //Rabbit mq
//        //Publish Order created Event
//        OrderCreatedEvent event = new OrderCreatedEvent(savedOrder.getId(),savedOrder.getUserId(),savedOrder.getStatus(), savedOrder.getTotalAmount(), mapToOrderItemDTOs(savedOrder.getItems()),savedOrder.getCreatedAt());
//
//        //rabbitTemplate.convertAndSend(exchangeName,routingKey, Map.of("orderId",savedOrder.getId(),"status","CREATED"));
//
//       // rabbitTemplate.convertAndSend(exchangeName,routingKey, event);
//
//        streamBridge.send("createOrder-out-0",event);
//
//
//        return Optional.of(mapToOrderResponse(savedOrder));
//
//    }
//
//    private List<OrderItemDTO> mapToOrderItemDTOs(List<OrderItem> items)
//    {
//       return items.stream().map(item->new OrderItemDTO(item.getId(),Long.valueOf(item.getProductId()),item.getQuantity(),item.getPrice(),item.getPrice().multiply(new BigDecimal(item.getQuantity())))).collect(Collectors.toList());
//    }
//
//    private OrderResponse mapToOrderResponse(Order savedOrder) {
//        return new OrderResponse(
//                savedOrder.getId(),
//                savedOrder.getTotalAmount(),
//                savedOrder.getStatus(),
//                savedOrder.getItems().stream()
//                        .map(orderItem -> new OrderItemDTO(
//                                orderItem.getId(),
//                                Long.valueOf(orderItem.getProductId()),
//                                orderItem.getQuantity(),
//                                orderItem.getPrice(),
//                                orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
//                        ))
//                        .toList(),
//                savedOrder.getCreatedAt()
//        );
//
//
//    }
//}

package com.ecommerce.order.service;

//import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dto.OrderCreatedEvent;
import com.ecommerce.order.dto.OrderItemDTO;
import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.model.Order;
import com.ecommerce.order.model.OrderItem;
import com.ecommerce.order.model.OrderStatus;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final StreamBridge streamBridge;
//    private final UserServiceClient userServiceClient;

    /**
     * Create an order for the given userId.
     * This method is transactional so DB save + cart clear happen atomically.
     */
    @Transactional
    public Optional<OrderResponse> createOrder(String userId) {
        // 1. get cart
        List<CartItem> cartItems = cartService.getCart(userId);
        if (cartItems.isEmpty()) return Optional.empty();

        // 2. validate user via client
//        var userResponse = userServiceClient.getUserDetails(userId);
//        if (userResponse == null) return Optional.empty();

        // 3. compute total
        BigDecimal totalPrice = cartItems.stream()
                .map(CartItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 4. build Order + items
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(OrderStatus.PENDING);
        //order.setStatus(OrderStatus.CONFIRMED);
        order.setTotalAmount(totalPrice);
        order.setCreatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = cartItems.stream()
                .map(item -> new OrderItem(null, item.getProductId(), item.getQuantity(), item.getPrice(), order))
                .collect(Collectors.toList());
        order.setItems(orderItems);

        // 5. persist order
        Order saved = orderRepository.save(order);

        // 6. clear cart
        cartService.clearCart(userId);

        // 7. create event (payload)
        OrderCreatedEvent event = new OrderCreatedEvent(
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

        // 8. correlation id + send message via StreamBridge
        String correlationId = UUID.randomUUID().toString();

        Message<OrderCreatedEvent> message = MessageBuilder.withPayload(event)
                .setHeader("correlationId", correlationId)
                .setHeader("traceId", correlationId) // optional: useful for tracing across services
                .build();

        // binding name "createOrder-out-0" must match your order service application.yml
        streamBridge.send("createOrder-out-0", message);

        // 9. return OrderResponse
        return Optional.of(mapToOrderResponse(saved));
    }

    private List<OrderItemDTO> mapToOrderItemDTOs(List<OrderItem> items) {
        return items.stream()
                .map(item -> new OrderItemDTO(
                        item.getId(),
                        Long.valueOf(item.getProductId()),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .collect(Collectors.toList());
    }

    private OrderResponse mapToOrderResponse(Order savedOrder) {
        return new OrderResponse(
                savedOrder.getId(),
                savedOrder.getTotalAmount(),
                savedOrder.getStatus(),
                mapToOrderItemDTOs(savedOrder.getItems()),
                savedOrder.getCreatedAt()
        );
    }

    public List<OrderResponse> getAllOrders()
    {
        List<Order>lst = orderRepository.findAll();
        List<OrderResponse>orderResponses = new ArrayList<>();
        for(Order o : lst)
        {
          OrderResponse or = mapToOrderResponse(o);
          orderResponses.add(or);

        }

        return orderResponses;
    }
}
