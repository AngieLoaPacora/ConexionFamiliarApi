package com.conexionfamiliar.service.impl;

import com.conexionfamiliar.dto.MessageRequest;
import com.conexionfamiliar.event.producer.KafkaProducerService;
import com.conexionfamiliar.model.entity.Message;
import com.conexionfamiliar.repository.MessageRepository;
import com.conexionfamiliar.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository repository;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final KafkaProducerService kafkaProducerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Enviar mensaje de texto simple
     */
    @Override
    public Mono<Message> send(MessageRequest request) {
        Message msg = Message.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .text(request.getText())
                .createdAt(LocalDateTime.now())
                .status(Message.MessageStatus.SENT)
                .build();

        return repository.save(msg)
                .flatMap(saved -> publishEvent("message.created", saved))
                .thenReturn(msg);
    }

    /**
     * Enviar mensaje con archivo multimedia (pendiente de implementar)
     */
    @Override
    public Mono<Message> createMediaMessage(Message msg, Flux<FilePart> fileParts) {
        // Por ahora solo guarda el mensaje sin archivo
        return repository.save(msg)
                .flatMap(saved -> publishEvent("message.created", saved).thenReturn(saved));
    }

    @Override
    public Mono<Message> markRead(String messageId, String readerId) {
        return repository.findById(messageId)
                .flatMap(msg -> {
                    msg.setStatus(Message.MessageStatus.READ);
                    return repository.save(msg)
                            .flatMap(saved -> publishEvent("message.read", saved).thenReturn(saved));
                });
    }

    @Override
    public Flux<Message> inbox(String receiverId) {
        return repository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    private Mono<Void> publishEvent(String topic, Message msg) {
        try {
            String payload = objectMapper.writeValueAsString(msg);
            return kafkaProducerService.publishMessageCreated(msg)
                    .then(redisTemplate.opsForValue().set("last_message:" + msg.getReceiverId(), payload))
                    .then();
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error al serializar el mensaje", e));
        }
    }
}
