package com.ecommerce.order.integration;

import com.ecommerce.order.dto.CartItemRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class OrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StreamBridge streamBridge;

    @Test
    void fullOrderFlowShouldSucceed() throws Exception {
        // Assumes user validation and product availability are successful internally
        when(streamBridge.send(anyString(), any())).thenReturn(true);
        // 1. Add to cart
        CartItemRequest request = new CartItemRequest(101L, 2);
        mockMvc.perform(post("/api/cart")
                        .header("X-User-ID", "user123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // 2. Create order
        mockMvc.perform(post("/api/orders")
                        .header("X-User-ID", "user123"))
                .andExpect(status().isCreated());

        // 3. Get orders
        mockMvc.perform(get("/api/orders")
                        .header("X-User-ID", "user123"))
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$[0].userId").value("user123"))
                .andExpect(jsonPath("$[0].items[0].productId").value("101"));
    }

    @Test
    void shouldFailIfUserIsInvalid() throws Exception {
        // Assumes internal logic throws InvalidUserException for user999

        mockMvc.perform(post("/api/orders")
                        .header("X-User-ID", "user999"))
                .andExpect(status().isBadRequest());
//                .andExpect(content().string("User is not authorized to place orders."));
    }

//    @Test
//    void shouldFailIfProductIsUnavailable() throws Exception {
//        // Assumes internal logic throws ProductUnavailableException for product 101
//
//        CartItemRequest request = new CartItemRequest(101L, 2);
//        mockMvc.perform(post("/api/cart")
//                        .header("X-User-ID", "user123")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
////                .andExpect(content().string("Product is unavailable."));
//    }
}