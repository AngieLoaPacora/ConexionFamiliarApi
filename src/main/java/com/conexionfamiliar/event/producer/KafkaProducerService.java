package com.conexionfamiliar.event.producer;

import com.conexionfamiliar.model.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import reactor.kafka.sender.SenderResult;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final SenderOptions<String, String> senderOptions;
    private final KafkaSender<String, String> sender;

    @Value("${kafka.topic.message-created}")
    private String topicMessageCreated;

    public Mono<SenderResult<Void>> publishMessageCreated(Message message) {
        String payload = String.format("{\"id\":\"%s\",\"senderId\":\"%s\",\"receiverId\":\"%s\",\"text\":\"%s\"}",
                message.getId(), message.getSenderId(), message.getReceiverId(), message.getText().replace("\"","'"));
        ProducerRecord<String, String> record = new ProducerRecord<>(topicMessageCreated, message.getId(), payload);
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(record, null);
        return sender.send(Mono.just(senderRecord)).next();
    }

    public Mono<SenderResult<Void>> publishMessageRead(Message message, String readerId) {
        String topic = topicMessageCreated + ".read";
        String payload = String.format("{\"id\":\"%s\",\"readerId\":\"%s\"}", message.getId(), readerId);
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, message.getId(), payload);
        SenderRecord<String, String, Void> senderRecord = SenderRecord.create(record, null);
        return sender.send(Mono.just(senderRecord)).next();
    }
}
