package com.conexionfamiliar.service;

import com.conexionfamiliar.model.entity.Message;
import com.conexionfamiliar.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final GridFsTemplate gridFsTemplate;
    private final ReactiveKafkaProducerTemplate<String, String> kafkaProducer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Crear mensaje de texto
    public Mono<Message> createTextMessage(Message msg) {
        msg.setCreatedAt(OffsetDateTime.now());
        msg.setStatus(msg.getScheduleAt() == null ? "SENT" : "SCHEDULED");

        return messageRepository.save(msg)
                .flatMap(saved -> publishEvent("message.created", saved))
                .thenReturn(msg);
    }

    // Crear mensaje con archivo multimedia
    public Mono<Message> createMediaMessage(Message msg, Flux<FilePart> fileParts) {
        return fileParts.next()
                .flatMap(filePart ->
                        storeInGridFs(filePart)
                                .map(gridfsId -> {
                                    msg.setContentRef(gridfsId);
                                    msg.setCreatedAt(OffsetDateTime.now());
                                    msg.setStatus("SENT");
                                    return msg;
                                })
                )
                .flatMap(messageRepository::save)
                .flatMap(saved -> publishEvent("message.created", saved).thenReturn(saved));
    }

    // Publicar evento a Kafka
    private Mono<Void> publishEvent(String topic, Message msg) {
        try {
            String payload = objectMapper.writeValueAsString(msg);
            return kafkaProducer.send(topic, payload).then();
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error al serializar el mensaje", e));
        }
    }

    // Guardar archivo en GridFS
    private Mono<String> storeInGridFs(FilePart filePart) {
        return Mono.fromCallable(() -> {
            try (InputStream inputStream = filePart.content()
                    .map(dataBuffer -> dataBuffer.asInputStream(true))
                    .blockFirst()) {
                return gridFsTemplate.store(
                        inputStream,
                        filePart.filename(),
                        filePart.headers().getContentType()
                ).toString();
            } catch (Exception e) {
                throw new RuntimeException("Error al almacenar el archivo en GridFS", e);
            }
        });
    }

    // Este método debe ir dentro de la clase
    public Flux<Message> getInbox(Long userId) {
        return messageRepository.findByReceiverId(userId);
    }
}
