package com.conexionfamiliar.service;

import com.conexionfamiliar.dto.UserRequest;
import com.conexionfamiliar.model.entity.User;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<User> register(UserRequest request);

    Mono<User> findById(String id);

    Mono<User> findByEmail(String email);
}
