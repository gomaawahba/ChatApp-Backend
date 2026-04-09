package com.gomaa.chatapp.controller;



import com.gomaa.chatapp.dto.request.AddMembersRequest;
import com.gomaa.chatapp.dto.request.CreateDirectConversationRequest;
import com.gomaa.chatapp.dto.request.CreateGroupConversationRequest;
import com.gomaa.chatapp.dto.response.ConversationResponse;
import com.gomaa.chatapp.service.ConversationService;
import com.gomaa.chatapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getMyConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userService.getByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(conversationService.getUserConversations(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable String id,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userService.getByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(conversationService.getConversation(id, userId));
    }

    @PostMapping("/direct")
    public ResponseEntity<ConversationResponse> createDirect(
            @Valid @RequestBody CreateDirectConversationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userService.getByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.createDirect(request, userId));
    }

    @PostMapping("/group")
    public ResponseEntity<ConversationResponse> createGroup(
            @Valid @RequestBody CreateGroupConversationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userService.getByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(conversationService.createGroup(request, userId));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ConversationResponse> addMembers(
            @PathVariable String id,
            @Valid @RequestBody AddMembersRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userService.getByUsername(userDetails.getUsername()).getId();
        return ResponseEntity.ok(conversationService.addMembers(id, request, userId));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable String id,
            @PathVariable String memberId,
            @AuthenticationPrincipal UserDetails userDetails) {
        String userId = userService.getByUsername(userDetails.getUsername()).getId();
        conversationService.removeMember(id, memberId, userId);
        return ResponseEntity.noContent().build();
    }
}
