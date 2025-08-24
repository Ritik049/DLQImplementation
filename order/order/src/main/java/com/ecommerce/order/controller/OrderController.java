package com.ecommerce.order.controller;


import com.ecommerce.order.dto.OrderResponse;
import com.ecommerce.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-ID") String userId)
    {
        Optional<OrderResponse> order = orderService.createOrder(userId);
        if(order.isEmpty()) return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(order.get(), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders()
    {     List<OrderResponse>lst = orderService.getAllOrders();
         return new ResponseEntity<>(lst,HttpStatus.OK);
    }
}
