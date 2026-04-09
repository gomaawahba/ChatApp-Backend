package com.gomaa.chatapp.dto.response;


import com.gomaa.chatapp.model.ConversationType;
import com.gomaa.chatapp.model.LastMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private ConversationType type;
    private String name;
    private String description;
    private String avatarUrl;
    private String adminId;
    private List<UserResponse> participants;
    private LastMessage lastMessage;
    private long unreadCount;
    private Instant createdAt;
    private Instant updatedAt;
}

