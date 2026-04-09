package com.gomaa.chatapp.exception;

import java.time.Instant;
import java.util.Map;

public class  ErrorResponse {
    public int status;
    public String message;
    public Instant timestamp = Instant.now();
    public Map<String, String> details;

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }
}


