package com.learning.book.config;

import com.learning.book.entity.Book;
import com.learning.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Inserts a few sample books on startup so there is data to play with. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }
        bookRepository.saveAll(List.of(
                Book.builder().title("Clean Code").author("Robert C. Martin").price(30.0).stock(10).build(),
                Book.builder().title("Effective Java").author("Joshua Bloch").price(45.0).stock(5).build(),
                Book.builder().title("Spring in Action").author("Craig Walls").price(40.0).stock(3).build()
        ));
        log.info("Loaded {} sample books", bookRepository.count());
    }
}
