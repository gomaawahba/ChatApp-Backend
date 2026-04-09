package com.gomaa.chatapp.dto.request;


import com.gomaa.chatapp.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank
    private String conversationId;

    @NotBlank
    private String content;

    private MessageType type = MessageType.TEXT;

    private String replyToMessageId;

    private String mediaUrl;
}
