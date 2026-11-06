package com.learning.book.dto;

import com.learning.book.entity.Book;

/** What the service returns. Never expose the entity directly. */
public record BookResponse(Long id, String title, String author, Double price, Integer stock) {

    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(),
                book.getPrice(), book.getStock());
    }
}
