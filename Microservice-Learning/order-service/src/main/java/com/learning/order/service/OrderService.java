package com.learning.order.service;

import com.learning.order.client.BookClient;
import com.learning.order.client.UserClient;
import com.learning.order.dto.BookDto;
import com.learning.order.dto.OrderRequest;
import com.learning.order.dto.OrderResponse;
import com.learning.order.dto.UserDto;
import com.learning.order.entity.Order;
import com.learning.order.entity.OrderStatus;
import com.learning.order.exception.BadRequestException;
import com.learning.order.exception.ResourceNotFoundException;
import com.learning.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final BookClient bookClient;   // Feign client
    private final UserClient userClient;   // Feign client

    public OrderResponse placeOrder(OrderRequest request) {
        // 1. Does the user exist? (call to user-service, 404 bubbles up)
        UserDto user = userClient.getUser(request.userId());
        log.info("User found: {}", user.name());

        // 2. Does the book exist and have enough stock? (call to book-service)
        BookDto book = bookClient.getBook(request.bookId());
        if (book.stock() < request.quantity()) {
            throw new BadRequestException("Not enough stock for '" + book.title()
                    + "'. Available: " + book.stock() + ", requested: " + request.quantity());
        }

        // 3. Simple price calculation
        double total = book.price() * request.quantity();

        // 4. Ask book-service to reduce stock
        bookClient.reduceStock(book.id(), request.quantity());

        // 5. Save the order in OUR database
        Order order = Order.builder()
                .userId(user.id())
                .bookId(book.id())
                .quantity(request.quantity())
                .totalPrice(total)
                .status(OrderStatus.PLACED)
                .createdAt(LocalDateTime.now())
                .build();
        Order saved = orderRepository.save(order);
        log.info("Order {} placed: user={} book='{}' qty={} total={}",
                saved.getId(), user.name(), book.title(), request.quantity(), total);
        return OrderResponse.from(saved);
    }

    public List<OrderResponse> getAll() {
        return orderRepository.findAll().stream().map(OrderResponse::from).toList();
    }

    public OrderResponse getById(Long id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id " + id));
    }
}
