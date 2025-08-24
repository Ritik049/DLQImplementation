package com.ecommerce.order.repository;

import com.ecommerce.order.model.CartItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void shouldFindByUserIdAndProductId() {
        // Arrange
        CartItem item = new CartItem("user123", "101", 2, BigDecimal.valueOf(500));
        cartItemRepository.save(item);

        // Act
        CartItem found = cartItemRepository.findByUserIdAndProductId("user123", "101");

        // Assert
        assertNotNull(found);
        assertEquals("101", found.getProductId());
        assertEquals("user123", found.getUserId());
    }

    @Test
    void shouldFindAllByUserId() {
        // Arrange
        cartItemRepository.save(new CartItem("user123", "101", 2, BigDecimal.valueOf(500)));
        cartItemRepository.save(new CartItem("user123", "102", 1, BigDecimal.valueOf(300)));
        cartItemRepository.save(new CartItem("user456", "103", 1, BigDecimal.valueOf(200)));

        // Act
        List<CartItem> items = cartItemRepository.findByUserId("user123");

        // Assert
        assertEquals(2, items.size());
        assertTrue(items.stream().anyMatch(item -> item.getProductId().equals("101")));
        assertTrue(items.stream().anyMatch(item -> item.getProductId().equals("102")));
    }

    @Test
    void shouldDeleteByUserId() {
        // Arrange
        cartItemRepository.save(new CartItem("user123", "101", 2, BigDecimal.valueOf(500)));
        cartItemRepository.save(new CartItem("user123", "102", 1, BigDecimal.valueOf(300)));

        // Act
        cartItemRepository.deleteByUserId("user123");

        // Assert
        List<CartItem> remaining = cartItemRepository.findByUserId("user123");
        assertTrue(remaining.isEmpty());
    }
}