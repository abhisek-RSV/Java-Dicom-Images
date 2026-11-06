package com.learning.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull Long userId,
        @NotNull Long bookId,
        @NotNull @Min(1) Integer quantity) {
}
