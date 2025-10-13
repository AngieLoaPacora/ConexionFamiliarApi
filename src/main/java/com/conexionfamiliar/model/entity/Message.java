package com.conexionfamiliar.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "messages")

public class Message {

    @Id

    private Long id;
    private Long senderId;
    private Long targetUserId;
    private String title;
    private String text;
    private String contentRef;  // gridfs id as String
    private OffsetDateTime scheduleAt;
    private OffsetDateTime createdAt;
    private String type;  // text, audio, image
    private String status;  // Sent, scheduled, read
    }
