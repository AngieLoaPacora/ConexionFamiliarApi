package com.conexionfamiliar.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
public class Message {

    @Id
    private String id;
    private String senderId;
    private String receiverId;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private MessageStatus status;

    public enum MessageStatus {
        PENDING,
        SENT,
        READ,
        DELIVERED
    }
}