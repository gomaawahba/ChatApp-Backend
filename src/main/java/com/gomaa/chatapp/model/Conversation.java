package com.gomaa.chatapp.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    private String id;

    @Builder.Default
    private ConversationType type = ConversationType.DIRECT;

    private String name;
    private String description;
    private String avatarUrl;
    private String adminId;

    @Builder.Default
    private List<String> participantIds = new ArrayList<>();

    private LastMessage lastMessage;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
