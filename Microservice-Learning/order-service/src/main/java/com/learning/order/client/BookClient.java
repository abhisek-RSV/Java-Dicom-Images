package com.learning.order.client;

import com.learning.order.dto.BookDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for book-service.
 * We only declare the interface - Feign generates the HTTP code at runtime.
 * The URL is hardcoded for now; Phase 3 (Eureka) will replace it with the service name.
 */
@FeignClient(name = "book-service")
public interface BookClient {

    @GetMapping("/books/{id}")
    BookDto getBook(@PathVariable("id") Long id);

    @PutMapping("/books/{id}/reduce-stock")
    BookDto reduceStock(@PathVariable("id") Long id, @RequestParam("qty") int qty);
}
