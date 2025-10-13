package com.conexionfamiliar.controller;

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

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<ResponseEntity<Message>> create(@RequestPart("meta") Mono<Message> meta,
                                                @RequestPart(value = "file", required = false) Flux<FilePart> file) {

        return meta.flatMap(m -> {
            if (file != null) {
                return messageService.createMediaMessage(m, file);
            } else {
                return messageService.createTextMessage(m);
            }
        }).map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(saved));
    }

    @GetMapping
    public Flux<Message> inbox(@RequestParam Long userId,
                               @RequestParam(defaultValue = "20") int limit) {
        return messageService.getInbox(userId).take(limit);
    }
}
