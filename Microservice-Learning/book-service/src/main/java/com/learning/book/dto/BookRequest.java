package com.learning.book.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** What the client sends when creating/updating a book. */
public record BookRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotNull @Positive Double price,
        @NotNull @Min(0) Integer stock) {
}
