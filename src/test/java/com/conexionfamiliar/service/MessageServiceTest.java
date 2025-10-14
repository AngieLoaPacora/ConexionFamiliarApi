package com.conexionfamiliar.service;
import com.conexionfamiliar.dto.MessageRequest;
import com.conexionfamiliar.model.entity.Message;
import com.conexionfamiliar.repository.MessageRepository;
import com.conexionfamiliar.event.producer.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class MessageServiceTest {

    MessageRepository repository = Mockito.mock(MessageRepository.class);
    ReactiveRedisTemplate<String,String> redis = Mockito.mock(ReactiveRedisTemplate.class);
    KafkaProducerService kafka = Mockito.mock(KafkaProducerService.class);
    com.conexionfamiliar.service.impl.MessageServiceImpl service;

    @BeforeEach
    void setup() {
        service = new com.conexionfamiliar.service.impl.MessageServiceImpl(repository, redis, kafka);
    }

    @Test
    void sendMessage_savesAndPublishes() {
        Message msg = new Message();
        msg.setId("1");
        msg.setText("Hola");
        when(repository.save(any())).thenReturn(Mono.just(msg));
        when(redis.opsForValue()).thenReturn(null); // simplificado: improve for real tests
        when(kafka.publishMessageCreated(any())).thenReturn(Mono.empty());

        MessageRequest req = new MessageRequest();
        req.setSenderId("u1");
        req.setReceiverId("u2");
        req.setText("Hola");

        StepVerifier.create(service.send(req))
                .expectNextMatches(m -> m.getText().equals("Hola"))
                .verifyComplete();
    }
}
