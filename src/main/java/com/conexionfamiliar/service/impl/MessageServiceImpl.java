package com.conexionfamiliar.service.impl;

import com.conexionfamiliar.dto.MessageRequest;
import com.conexionfamiliar.model.entity.Message;
import com.conexionfamiliar.repository.MessageRepository;
import com.conexionfamiliar.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final GridFsTemplate gridFsTemplate;
    private final ReactiveKafkaProducerTemplate<String, String> kafkaProducer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Enviar mensaje de texto simple
     */
    @Override
    public Mono<Message> send(MessageRequest request) {
        Message msg = Message.builder()
                .senderId(request.getSenderId())
                .receiverId(request.getReceiverId())
                .text(request.getContent()) // mapeamos 'content' del DTO a 'text' de la entidad
                .createdAt(LocalDateTime.now())
                .status(Message.MessageStatus.SENT)
                .build();

        return messageRepository.save(msg)
                .flatMap(saved -> publishEvent("message.created", saved))
                .thenReturn(msg);
    }

    /**
     * Enviar mensaje con archivos multimedia
     */
    @Override
    public Mono<Message> createMediaMessage(Message msg, Flux<FilePart> fileParts) {
        return fileParts.next()
                .flatMap(filePart ->
                        storeInGridFs(filePart)
                                .map(gridfsId -> {
                                    msg.setText("Archivo almacenado con ID: " + gridfsId);
                                    msg.setCreatedAt(LocalDateTime.now());
                                    msg.setStatus(Message.MessageStatus.SENT);
                                    return msg;
                                })
                )
                .flatMap(messageRepository::save)
                .flatMap(saved -> publishEvent("message.created", saved).thenReturn(saved));
    }

    /**
     * Marcar mensaje como leído
     */
    @Override
    public Mono<Message> markRead(String messageId, String readerId) {
        return messageRepository.findById(messageId)
                .flatMap(msg -> {
                    msg.setStatus(Message.MessageStatus.READ);
                    return messageRepository.save(msg)
                            .flatMap(saved -> publishEvent("message.read", saved).thenReturn(saved));
                });
    }

    /**
     * Obtener mensajes recibidos por un usuario
     */
    @Override
    public Flux<Message> inbox(String receiverId) {
        return messageRepository.findByReceiverIdOrderByCreatedAtDesc(receiverId);
    }

    /**
     * Publicar evento en Kafka
     */
    private Mono<Void> publishEvent(String topic, Message msg) {
        try {
            String payload = objectMapper.writeValueAsString(msg);
            return kafkaProducer.send(topic, payload).then();
        } catch (JsonProcessingException e) {
            return Mono.error(new RuntimeException("Error al serializar el mensaje", e));
        }
    }

    /**
     * Guardar archivo en Mongo GridFS
     */
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
}
