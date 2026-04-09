package com.gomaa.chatapp.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDirectConversationRequest {

    @NotBlank
    private String targetUserId;
}
