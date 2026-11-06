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
import com.learning.order.exception.RemoteServiceException;
import com.learning.order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test: the other services are mocked, so no network calls happen.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    BookClient bookClient;
    @Mock
    UserClient userClient;

    @InjectMocks
    OrderService orderService;

    private final UserDto alice = new UserDto(1L, "Alice", "alice@example.com");
    private final BookDto book = new BookDto(1L, "Clean Code", "Robert C. Martin", 30.0, 10);

    @Test
    void placeOrder_calculatesTotalAndReducesStock() {
        when(userClient.getUser(1L)).thenReturn(alice);
        when(bookClient.getBook(1L)).thenReturn(book);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderResponse response = orderService.placeOrder(new OrderRequest(1L, 1L, 2));

        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.totalPrice()).isEqualTo(60.0);
        assertThat(response.status()).isEqualTo(OrderStatus.PLACED);
        verify(bookClient).reduceStock(1L, 2);
    }

    @Test
    void placeOrder_failsWhenNotEnoughStock() {
        when(userClient.getUser(1L)).thenReturn(alice);
        when(bookClient.getBook(1L)).thenReturn(book);

        assertThatThrownBy(() -> orderService.placeOrder(new OrderRequest(1L, 1L, 50)))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Not enough stock");

        verify(bookClient, never()).reduceStock(anyLong(), anyInt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_failsWhenUserDoesNotExist() {
        when(userClient.getUser(99L))
                .thenThrow(new RemoteServiceException(404, "User not found with id 99"));

        assertThatThrownBy(() -> orderService.placeOrder(new OrderRequest(99L, 1L, 1)))
                .isInstanceOf(RemoteServiceException.class)
                .hasMessage("User not found with id 99");

        verify(bookClient, never()).getBook(anyLong());
    }
}
