package com.conexionfamiliar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class MessageRequest {
    private String senderId;       // ID del usuario que envía
    private String receiverId;     // Id del receptor
    private String content;        // Texto del mensaje
    private String type;           // "TEXT", "IMAGE", "VIDEO", etc.
    private String text;
    private List<FilePart> files;  // Archivos adjuntos (opcional)
}