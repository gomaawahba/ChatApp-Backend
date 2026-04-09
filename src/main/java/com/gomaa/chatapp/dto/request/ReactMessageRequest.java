package com.gomaa.chatapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReactMessageRequest {

    @NotBlank
    private String emoji;
}
