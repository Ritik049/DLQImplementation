package com.ecommerce.order.service;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.model.CartItem;
import com.ecommerce.order.repository.CartItemRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class CartServiceTest {

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartService cartService;

    @Test
    void shouldAddNewItemToCart() {
        CartItemRequest request = new CartItemRequest(101L, 2);
        when(cartItemRepository.findByUserIdAndProductId("user123", "101")).thenReturn(null);

        boolean result = cartService.addToCart("user123", request);

        assertTrue(result);
        verify(cartItemRepository).save(any(CartItem.class));
    }

    @Test
    void shouldUpdateExistingItemQuantity() {
        CartItem existingItem = new CartItem("user123", "101", 1, BigDecimal.valueOf(500));
        CartItemRequest request = new CartItemRequest(101L, 2);
        when(cartItemRepository.findByUserIdAndProductId("user123", "101")).thenReturn(existingItem);

        boolean result = cartService.addToCart("user123", request);

        assertTrue(result);
        assertEquals(3, existingItem.getQuantity());
        verify(cartItemRepository).save(existingItem);
    }

    @Test
    void shouldDeleteItemFromCartSuccessfully() {
        CartItem item = new CartItem("user123", "101", 2, BigDecimal.valueOf(500));
        when(cartItemRepository.findByUserIdAndProductId("user123", "101")).thenReturn(item);

        boolean result = cartService.deleteItemFromCart("user123", 101L);

        assertTrue(result);
        verify(cartItemRepository).delete(item);
    }

    @Test
    void shouldReturnFalseWhenDeletingNonExistentItem() {
        when(cartItemRepository.findByUserIdAndProductId("user123", "999")).thenReturn(null);

        boolean result = cartService.deleteItemFromCart("user123", 999L);

        assertFalse(result);
        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void shouldReturnCartItemsForUser() {
        List<CartItem> mockItems = List.of(
                new CartItem("user123", "101", 2, BigDecimal.valueOf(500)),
                new CartItem("user123", "102", 1, BigDecimal.valueOf(300))
        );
        when(cartItemRepository.findByUserId("user123")).thenReturn(mockItems);

        List<CartItem> result = cartService.getCart("user123");

        assertEquals(2, result.size());
        assertEquals("101", result.get(0).getProductId());
        assertEquals("102", result.get(1).getProductId());
    }


    @Test
    void shouldClearCartSuccessfully() {
        cartService.clearCart("user123");

        // Verify the service calls the correct repository method
        verify(cartItemRepository).deleteByUserId("user123");
    }

}
