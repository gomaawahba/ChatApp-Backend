package com.gomaa.chatapp.controller;


import com.gomaa.chatapp.dto.request.EditMessageRequest;
import com.gomaa.chatapp.dto.request.ReactMessageRequest;
import com.gomaa.chatapp.dto.request.SendMessageRequest;
import com.gomaa.chatapp.dto.response.MessageResponse;
import com.gomaa.chatapp.dto.response.PageResponse;
import com.gomaa.chatapp.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            @Valid @RequestBody SendMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(request, userDetails.getUsername()));
    }

    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(
            @PathVariable String conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                messageService.getMessages(conversationId, userDetails.getUsername(), page, size));
    }

    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> editMessage(
            @PathVariable String messageId,
            @Valid @RequestBody EditMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.editMessage(messageId, request, userDetails.getUsername()));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable String messageId,
            @AuthenticationPrincipal UserDetails userDetails) {
        messageService.deleteMessage(messageId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{messageId}/reactions")
    public ResponseEntity<MessageResponse> reactToMessage(
            @PathVariable String messageId,
            @Valid @RequestBody ReactMessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(messageService.reactToMessage(messageId, request, userDetails.getUsername()));
    }

    @PostMapping("/conversation/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable String conversationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        messageService.markAsRead(conversationId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}
