package com.conexionfamiliar.controller;

import com.conexionfamiliar.dto.MessageRequest;
import com.conexionfamiliar.model.entity.Message;
import com.conexionfamiliar.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Enviar mensaje (texto o multimedia)
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<ResponseEntity<Message>> sendMessage(
            @ModelAttribute MessageRequest request,
            @RequestPart(value = "files", required = false) Flux<FilePart> files) {

        // Si vienen archivos, tratamos como multimedia
        if (files != null) {
            Message msg = Message.builder()
                    .senderId(request.getSenderId())
                    .receiverId(request.getReceiverId())
                    .text("Archivo adjunto")
                    .build();
            return messageService.createMediaMessage(msg, files)
                    .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
        }

        // Si no hay archivos, tratamos como texto simple
        return messageService.send(request)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    // Obtener mensajes de la bandeja de entrada
    @GetMapping("/{receiverId}")
    public Flux<Message> inbox(@PathVariable String receiverId,
                               @RequestParam(defaultValue = "20") int limit) {
        return messageService.inbox(receiverId).take(limit);
    }

    // Marcar mensaje como leído
    @PatchMapping("/{messageId}/read")
    public Mono<ResponseEntity<Message>> markAsRead(@PathVariable String messageId,
                                                    @RequestParam String readerId) {
        return messageService.markRead(messageId, readerId)
                .map(updated -> ResponseEntity.ok(updated));
    }
}
