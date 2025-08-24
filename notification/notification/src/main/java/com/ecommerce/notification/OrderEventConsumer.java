package com.ecommerce.notification;

import com.ecommerce.notification.payload.OrderCreatedEvent;
import com.ecommerce.notification.payload.OrderStatus;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.function.Consumer;

@Slf4j
@Service
public class OrderEventConsumer {

//    @RabbitListener(queues = "${rabbitmq.queue.name}")
//    public void handleOrderEvent(OrderCreatedEvent orderEvent)
//    {
//        System.out.println("Received Order Event: "+orderEvent);
//
//        Long orderId  = orderEvent.getOrderId();
//        OrderStatus orderStatus = orderEvent.getStatus();
//
//        System.out.println("Order ID: "+orderId);
//        System.out.println("Order Status: "+ orderStatus);
//
//        //Update Database
//        //Send Notification
//        //Send Emails
//        //Generate Invoice
//        //Send Seller Notification
//    }
//
////    @RabbitListener(queues = "${rabbitmq.queue.name}")
////    public void handleOrderEvent(Map<String,Object> orderEvent)
////    {
////        System.out.println("Received Order Event: "+orderEvent);
////
////        Long orderId  = Long.valueOf(orderEvent.get("orderId").toString());
////        String orderStatus = orderEvent.get("status").toString();
////
////        System.out.println("Order ID: "+orderId);
////        System.out.println("Order Status: "+ orderStatus);
////
////        //Update Database
////        //Send Notification
////        //Send Emails
////        //Generate Invoice
////        //Send Seller Notification
////    }

    @Bean
    public Consumer<OrderCreatedEvent> orderCreated()
    {
        return event->{
            log.info("Received order created event for order: {}",event.getOrderId());
            log.info("Received order created event for user id: {}", event.getUserId());
            log.info("Received order created event for user id: {}",event.getStatus());


        };
    }

}
