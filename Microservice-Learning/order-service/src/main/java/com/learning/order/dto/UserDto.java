package com.learning.order.dto;

/** order-service's own copy of the user shape returned by user-service. */
public record UserDto(Long id, String name, String email) {
}
