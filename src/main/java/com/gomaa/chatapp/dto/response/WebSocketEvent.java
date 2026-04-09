package com.gomaa.chatapp.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketEvent {

    private EventType type;
    private Object payload;
    private Instant timestamp;

    public enum EventType {
        NEW_MESSAGE,
        MESSAGE_UPDATED,
        MESSAGE_DELETED,
        MESSAGE_READ,
        USER_TYPING,
        USER_STOP_TYPING,
        USER_ONLINE,
        USER_OFFLINE,
        CONVERSATION_CREATED,
        MEMBER_ADDED,
        MEMBER_REMOVED
    }

    public static WebSocketEvent of(EventType type, Object payload) {
        return WebSocketEvent.builder()
                .type(type)
                .payload(payload)
                .timestamp(Instant.now())
                .build();
    }
}
