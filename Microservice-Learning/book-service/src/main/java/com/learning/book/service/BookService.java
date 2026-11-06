package com.learning.book.service;

import com.learning.book.dto.BookRequest;
import com.learning.book.dto.BookResponse;
import com.learning.book.entity.Book;
import com.learning.book.exception.BadRequestException;
import com.learning.book.exception.ResourceNotFoundException;
import com.learning.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<BookResponse> getAll() {
        return bookRepository.findAll().stream().map(BookResponse::from).toList();
    }

    public BookResponse getById(Long id) {
        return BookResponse.from(findBook(id));
    }

    public BookResponse create(BookRequest request) {
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .price(request.price())
                .stock(request.stock())
                .build();
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = findBook(id);
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setPrice(request.price());
        book.setStock(request.stock());
        return BookResponse.from(book);
    }

    public void delete(Long id) {
        bookRepository.delete(findBook(id));
    }

    /** Used later by order-service (Phase 2) when an order is placed. */
    @Transactional
    public BookResponse reduceStock(Long id, int qty) {
        if (qty <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        Book book = findBook(id);
        if (book.getStock() < qty) {
            throw new BadRequestException(
                    "Not enough stock for book " + id + ". Available: " + book.getStock());
        }
        book.setStock(book.getStock() - qty);
        return BookResponse.from(book);
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
    }
}
