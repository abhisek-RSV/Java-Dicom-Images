package com.learning.order.dto;

/**
 * order-service's own copy of the book shape returned by book-service.
 * Services do NOT share Java classes - each one defines what it needs.
 */
public record BookDto(Long id, String title, String author, Double price, Integer stock) {
}
