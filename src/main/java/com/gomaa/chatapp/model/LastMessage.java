package com.gomaa.chatapp.model;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LastMessage {
    private String content;
    private String senderId;
    private String senderUsername;
    private Instant timestamp;
    private MessageType type;
}
