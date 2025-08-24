package com.ecommerce.order.controller;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldAddToCartSuccessfully() throws Exception {
        CartItemRequest request = new CartItemRequest(101l, 2);
        when(cartService.addToCart("user123", request)).thenReturn(true);

        mockMvc.perform(post("/api/cart")
                        .header("X-User-ID", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldFailToAddToCart() throws Exception {
        CartItemRequest request = new CartItemRequest(101l, 2);
        when(cartService.addToCart("user123", request)).thenReturn(false);

        mockMvc.perform(post("/api/cart")
                        .header("X-User-ID", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Not able to complete request | Product Out of Stock or User not found or Product not found"));
    }

    @Test
    void shouldGetCartItems() throws Exception {
        List<CartItem> cartItems = List.of(
                new CartItem("user123", "101", 2, BigDecimal.valueOf(500)),
                new CartItem("user123", "102", 1, BigDecimal.valueOf(300))
        );
        when(cartService.getCart("user123")).thenReturn(cartItems);

        mockMvc.perform(get("/api/cart")
                        .header("X-User-ID", "user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldRemoveItemFromCartSuccessfully() throws Exception {
        when(cartService.deleteItemFromCart("user123", 101L)).thenReturn(true);

        mockMvc.perform(delete("/api/cart/items/101")
                        .header("X-User-ID", "user123"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenItemNotInCart() throws Exception {
        when(cartService.deleteItemFromCart("user123", 999L)).thenReturn(false);

        mockMvc.perform(delete("/api/cart/items/999")
                        .header("X-User-ID", "user123"))
                .andExpect(status().isNotFound());
    }
}