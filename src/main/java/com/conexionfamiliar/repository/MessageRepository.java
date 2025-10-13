package com.conexionfamiliar.repository;

import com.conexionfamiliar.model.entity.Message;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface MessageRepository extends ReactiveCrudRepository<Message, Long> {
    Flux<Message> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);
    Flux<Message> findByStatus(String status);
    Flux<Message> findByReceiverId(Long receiverId);
}

