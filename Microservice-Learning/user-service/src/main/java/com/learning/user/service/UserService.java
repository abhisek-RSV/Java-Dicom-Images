package com.learning.user.service;

import com.learning.user.dto.UserRequest;
import com.learning.user.dto.UserResponse;
import com.learning.user.entity.User;
import com.learning.user.exception.BadRequestException;
import com.learning.user.exception.ResourceNotFoundException;
import com.learning.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(UserResponse::from).toList();
    }

    public UserResponse getById(Long id) {
        return UserResponse.from(findUser(id));
    }

    public UserResponse create(UserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email already registered: " + request.email());
        }
        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .build();
        return UserResponse.from(userRepository.save(user));
    }

    public void delete(Long id) {
        userRepository.delete(findUser(id));
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }
}
