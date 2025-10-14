package com.conexionfamiliar.service;

import com.conexionfamiliar.dto.MessageRequest;
import com.conexionfamiliar.model.entity.Message;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MessageService {

    // Enviar mensaje de texto o simple
    Mono<Message> send(MessageRequest request);

    // Enviar mensaje multimedia (imagen, video, etc.)
    Mono<Message> createMediaMessage(Message msg, Flux<FilePart> fileParts);

    // Marcar un mensaje como leído
    Mono<Message> markRead(String messageId, String readerId);

    // Consultar la bandeja de entrada de un usuario
    Flux<Message> inbox(String receiverId);
}
