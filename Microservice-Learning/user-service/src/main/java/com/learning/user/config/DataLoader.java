package com.learning.user.config;

import com.learning.user.entity.User;
import com.learning.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Inserts a few sample users on startup. */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        userRepository.saveAll(List.of(
                User.builder().name("Alice").email("alice@example.com").build(),
                User.builder().name("Bob").email("bob@example.com").build()
        ));
        log.info("Loaded {} sample users", userRepository.count());
    }
}
