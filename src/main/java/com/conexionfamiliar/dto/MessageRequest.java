package com.conexionfamiliar.dto;

import lombok.Data;
import org.springframework.http.codec.multipart.FilePart;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageRequest {
    private String senderId;       // ID del usuario que envía
    private String receiverId;     // ID del receptor
    private String content;        // Texto del mensaje
    private String type;           // "TEXT", "IMAGE", "VIDEO", etc.
    private List<FilePart> files;  // Archivos adjuntos (opcional)
}