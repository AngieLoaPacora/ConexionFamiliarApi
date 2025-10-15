package com.conexionfamiliar.service;

import com.conexionfamiliar.dto.MessageRequest;
import com.conexionfamiliar.event.producer.KafkaProducerService;
import com.conexionfamiliar.model.entity.Message;
import com.conexionfamiliar.repository.MessageRepository;
import com.conexionfamiliar.service.impl.MessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository repository;

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    @Mock
    private KafkaProducerService kafkaProducerService;

    private MessageServiceImpl service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new MessageServiceImpl(repository, redisTemplate, kafkaProducerService);
    }

    @Test
    void send_shouldSaveAndPublishMessage() {
        // Datos simulados
        MessageRequest req = new MessageRequest();
        req.setSenderId("papa");
        req.setReceiverId("nina");
        req.setText("Hola, te quiero mucho ❤️");

        Message savedMessage = Message.builder()
                .id("1")
                .senderId("papa")
                .receiverId("nina")
                .text("Hola, te quiero mucho ❤️")
                .build();

        // Simulaciones
        when(repository.save(any())).thenReturn(Mono.just(savedMessage));
        when(kafkaProducerService.publishMessageCreated(any())).thenReturn(Mono.empty());
        when(valueOps.set(any(), any())).thenReturn(Mono.just(true));

        // Verificación reactiva
        StepVerifier.create(service.send(req))
                .expectNextMatches(msg -> msg.getText().contains("Hola"))
                .verifyComplete();
    }
}
