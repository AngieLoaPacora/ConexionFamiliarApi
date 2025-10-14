package com.conexionfamiliar.service.impl;

import com.conexionfamiliar.dto.UserRequest;
import com.conexionfamiliar.model.entity.User;
import com.conexionfamiliar.repository.UserRepository;
import com.conexionfamiliar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Mono<User> register(UserRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // Aquí luego deberías encriptar la contraseña
                .role("USER")
                .build();

        return userRepository.save(user);
    }

    @Override
    public Mono<User> findById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
