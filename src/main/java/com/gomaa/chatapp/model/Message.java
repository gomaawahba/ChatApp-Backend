package com.gomaa.chatapp.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    private String id;

    @Indexed
    private String conversationId;

    private String senderId;
    private String senderUsername;
    private String content;

    @Builder.Default
    private MessageType type = MessageType.TEXT;

    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    private String mediaUrl;

    @Builder.Default
    private List<Reaction> reactions = new ArrayList<>();

    private String replyToMessageId;
    private boolean edited;
    private Instant editedAt;

    @CreatedDate
    private Instant createdAt;
}
