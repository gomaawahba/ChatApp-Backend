package com.gomaa.chatapp.service;

import com.gomaa.chatapp.dto.request.EditMessageRequest;
import com.gomaa.chatapp.dto.request.ReactMessageRequest;
import com.gomaa.chatapp.dto.request.SendMessageRequest;
import com.gomaa.chatapp.dto.response.MessageResponse;
import com.gomaa.chatapp.dto.response.PageResponse;
import com.gomaa.chatapp.dto.response.WebSocketEvent;
import com.gomaa.chatapp.exception.AccessDeniedException;
import com.gomaa.chatapp.exception.ResourceNotFoundException;
import com.gomaa.chatapp.model.*;
import com.gomaa.chatapp.repository.ConversationRepository;
import com.gomaa.chatapp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageResponse sendMessage(SendMessageRequest request, String senderUsername) {
        User sender = userService.getEntityByUsername(senderUsername);

        Conversation conv = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        if (!conv.getParticipantIds().contains(sender.getId())) {
            throw new AccessDeniedException("You are not a participant in this conversation");
        }

        Message message = Message.builder()
                .conversationId(request.getConversationId())
                .senderId(sender.getId())
                .senderUsername(sender.getUsername())
                .content(request.getContent())
                .type(request.getType())
                .mediaUrl(request.getMediaUrl())
                .replyToMessageId(request.getReplyToMessageId())
                .build();

        Message saved = messageRepository.save(message);

        // Update conversation last message
        LastMessage lastMsg = LastMessage.builder()
                .content(saved.getContent())
                .senderId(saved.getSenderId())
                .senderUsername(saved.getSenderUsername())
                .timestamp(saved.getCreatedAt())
                .type(saved.getType())
                .build();
        conv.setLastMessage(lastMsg);
        conversationRepository.save(conv);

        MessageResponse response = MessageResponse.from(saved);

        // Broadcast to all participants via WebSocket
        WebSocketEvent event = WebSocketEvent.of(WebSocketEvent.EventType.NEW_MESSAGE, response);
        conv.getParticipantIds().forEach(participantId ->
                messagingTemplate.convertAndSendToUser(participantId, "/queue/messages", event)
        );

        // Also broadcast to conversation topic
        messagingTemplate.convertAndSend("/topic/conversation." + conv.getId(), event);

        log.debug("Message sent in conversation {} by {}", conv.getId(), senderUsername);
        return response;
    }

    public PageResponse<MessageResponse> getMessages(String conversationId, String userId, int page, int size) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));

        User user = userService.getEntityByUsername(userId);
        if (!conv.getParticipantIds().contains(user.getId())) {
            throw new AccessDeniedException("You are not a participant in this conversation");
        }

        Page<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, PageRequest.of(page, size));

        Page<MessageResponse> responsePage = messages.map(MessageResponse::from);
        return PageResponse.from(responsePage);
    }

    public MessageResponse editMessage(String messageId, EditMessageRequest request, String username) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        User user = userService.getEntityByUsername(username);
        if (!message.getSenderId().equals(user.getId())) {
            throw new AccessDeniedException("You can only edit your own messages");
        }

        message.setContent(request.getContent());
        message.setEdited(true);
        message.setEditedAt(Instant.now());
        Message saved = messageRepository.save(message);

        MessageResponse response = MessageResponse.from(saved);
        WebSocketEvent event = WebSocketEvent.of(WebSocketEvent.EventType.MESSAGE_UPDATED, response);
        messagingTemplate.convertAndSend("/topic/conversation." + message.getConversationId(), event);

        return response;
    }

    public void deleteMessage(String messageId, String username) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        User user = userService.getEntityByUsername(username);
        if (!message.getSenderId().equals(user.getId())) {
            throw new AccessDeniedException("You can only delete your own messages");
        }

        String conversationId = message.getConversationId();
        messageRepository.delete(message);

        WebSocketEvent event = WebSocketEvent.of(WebSocketEvent.EventType.MESSAGE_DELETED,
                java.util.Map.of("messageId", messageId, "conversationId", conversationId));
        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, event);
    }

    public MessageResponse reactToMessage(String messageId, ReactMessageRequest request, String username) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found"));

        User user = userService.getEntityByUsername(username);

        // Remove existing reaction from this user, then add new one
        message.getReactions().removeIf(r -> r.getUserId().equals(user.getId()));
        message.getReactions().add(new Reaction(user.getId(), request.getEmoji()));

        Message saved = messageRepository.save(message);
        MessageResponse response = MessageResponse.from(saved);

        WebSocketEvent event = WebSocketEvent.of(WebSocketEvent.EventType.MESSAGE_UPDATED, response);
        messagingTemplate.convertAndSend("/topic/conversation." + message.getConversationId(), event);

        return response;
    }

    public void markAsRead(String conversationId, String username) {
        User user = userService.getEntityByUsername(username);

        List<Message> unread = messageRepository.findByConversationIdAndStatusNot(
                conversationId, MessageStatus.READ);

        // Only mark messages sent by others as read
        List<Message> toMark = unread.stream()
                .filter(m -> !m.getSenderId().equals(user.getId()))
                .peek(m -> m.setStatus(MessageStatus.READ))
                .collect(Collectors.toList());

        if (!toMark.isEmpty()) {
            messageRepository.saveAll(toMark);

            WebSocketEvent event = WebSocketEvent.of(WebSocketEvent.EventType.MESSAGE_READ,
                    java.util.Map.of("conversationId", conversationId, "userId", user.getId()));
            messagingTemplate.convertAndSend("/topic/conversation." + conversationId, event);
        }
    }
}
