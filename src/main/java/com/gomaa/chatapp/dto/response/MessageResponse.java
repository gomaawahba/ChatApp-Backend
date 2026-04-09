package com.gomaa.chatapp.dto.response;


import com.gomaa.chatapp.model.Message;
import com.gomaa.chatapp.model.MessageStatus;
import com.gomaa.chatapp.model.MessageType;
import com.gomaa.chatapp.model.Reaction;
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
public class MessageResponse {
    private String id;
    private String conversationId;
    private String senderId;
    private String senderUsername;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private String mediaUrl;
    private List<Reaction> reactions;
    private String replyToMessageId;
    private boolean edited;
    private Instant editedAt;
    private Instant createdAt;

    public static MessageResponse from(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderId(message.getSenderId())
                .senderUsername(message.getSenderUsername())
                .content(message.getContent())
                .type(message.getType())
                .status(message.getStatus())
                .mediaUrl(message.getMediaUrl())
                .reactions(message.getReactions())
                .replyToMessageId(message.getReplyToMessageId())
                .edited(message.isEdited())
                .editedAt(message.getEditedAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
