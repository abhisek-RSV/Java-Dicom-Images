package com.learning.order.dto;

import com.learning.order.entity.Order;
import com.learning.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(Long id, Long userId, Long bookId, Integer quantity,
                            Double totalPrice, OrderStatus status, LocalDateTime createdAt) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getUserId(), order.getBookId(),
                order.getQuantity(), order.getTotalPrice(), order.getStatus(), order.getCreatedAt());
    }
}
