package com.gomaa.chatapp.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateGroupConversationRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    private String name;

    private String description;

    @NotEmpty
    private List<String> memberIds;
}
