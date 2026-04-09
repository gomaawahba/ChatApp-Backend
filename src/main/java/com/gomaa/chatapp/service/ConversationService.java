package com.gomaa.chatapp.service;



import com.gomaa.chatapp.dto.request.AddMembersRequest;
import com.gomaa.chatapp.dto.request.CreateDirectConversationRequest;
import com.gomaa.chatapp.dto.request.CreateGroupConversationRequest;
import com.gomaa.chatapp.dto.response.ConversationResponse;
import com.gomaa.chatapp.dto.response.UserResponse;
import com.gomaa.chatapp.exception.AccessDeniedException;
import com.gomaa.chatapp.exception.ResourceNotFoundException;
import com.gomaa.chatapp.model.Conversation;
import com.gomaa.chatapp.model.ConversationType;
import com.gomaa.chatapp.model.MessageStatus;
import com.gomaa.chatapp.model.User;
import com.gomaa.chatapp.repository.ConversationRepository;
import com.gomaa.chatapp.repository.MessageRepository;
import com.gomaa.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public List<ConversationResponse> getUserConversations(String userId) {
        List<Conversation> conversations =
                conversationRepository.findByParticipantIdsContainingOrderByUpdatedAtDesc(userId);

        return conversations.stream()
                .map(c -> toResponse(c, userId))
                .collect(Collectors.toList());
    }

    public ConversationResponse getConversation(String conversationId, String userId) {
        Conversation conv = findAndVerifyAccess(conversationId, userId);
        return toResponse(conv, userId);
    }

    public ConversationResponse createDirect(CreateDirectConversationRequest request, String currentUserId) {
        // Check if direct conversation already exists
        return conversationRepository.findDirectConversation(currentUserId, request.getTargetUserId())
                .map(existing -> toResponse(existing, currentUserId))
                .orElseGet(() -> {
                    if (!userRepository.existsById(request.getTargetUserId())) {
                        throw new ResourceNotFoundException("Target user not found");
                    }
                    Conversation conv = Conversation.builder()
                            .type(ConversationType.DIRECT)
                            .participantIds(List.of(currentUserId, request.getTargetUserId()))
                            .build();
                    Conversation saved = conversationRepository.save(conv);
                    log.info("Direct conversation created: {}", saved.getId());
                    return toResponse(saved, currentUserId);
                });
    }

    public ConversationResponse createGroup(CreateGroupConversationRequest request, String currentUserId) {
        List<String> memberIds = new ArrayList<>(request.getMemberIds());
        if (!memberIds.contains(currentUserId)) {
            memberIds.add(currentUserId);
        }

        // Validate all members exist
        List<User> members = userRepository.findByIdIn(memberIds);
        if (members.size() != memberIds.size()) {
            throw new ResourceNotFoundException("One or more users not found");
        }

        Conversation conv = Conversation.builder()
                .type(ConversationType.GROUP)
                .name(request.getName())
                .description(request.getDescription())
                .adminId(currentUserId)
                .participantIds(memberIds)
                .build();

        Conversation saved = conversationRepository.save(conv);
        log.info("Group conversation '{}' created by {}", saved.getName(), currentUserId);
        return toResponse(saved, currentUserId);
    }

    public ConversationResponse addMembers(String conversationId, AddMembersRequest request, String currentUserId) {
        Conversation conv = findAndVerifyAccess(conversationId, currentUserId);

        if (conv.getType() != ConversationType.GROUP) {
            throw new IllegalArgumentException("Cannot add members to a direct conversation");
        }
        if (!currentUserId.equals(conv.getAdminId())) {
            throw new AccessDeniedException("Only the group admin can add members");
        }

        List<String> newMembers = request.getMemberIds().stream()
                .filter(id -> !conv.getParticipantIds().contains(id))
                .collect(Collectors.toList());

        if (!newMembers.isEmpty()) {
            conv.getParticipantIds().addAll(newMembers);
            conversationRepository.save(conv);
        }

        return toResponse(conv, currentUserId);
    }

    public void removeMember(String conversationId, String memberId, String currentUserId) {
        Conversation conv = findAndVerifyAccess(conversationId, currentUserId);

        if (conv.getType() != ConversationType.GROUP) {
            throw new IllegalArgumentException("Cannot remove members from a direct conversation");
        }
        boolean isAdmin = currentUserId.equals(conv.getAdminId());
        boolean isSelf = currentUserId.equals(memberId);

        if (!isAdmin && !isSelf) {
            throw new AccessDeniedException("Only admin can remove other members");
        }

        conv.getParticipantIds().remove(memberId);
        conversationRepository.save(conv);
    }

    private Conversation findAndVerifyAccess(String conversationId, String userId) {
        Conversation conv = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
        if (!conv.getParticipantIds().contains(userId)) {
            throw new AccessDeniedException("You are not a participant in this conversation");
        }
        return conv;
    }

    private ConversationResponse toResponse(Conversation conv, String currentUserId) {
        List<User> participants = userRepository.findByIdIn(conv.getParticipantIds());
        List<UserResponse> participantResponses = participants.stream()
                .map(UserResponse::from)
                .collect(Collectors.toList());

        long unreadCount = messageRepository.countByConversationIdAndStatusNot(
                conv.getId(), MessageStatus.READ);

        return ConversationResponse.builder()
                .id(conv.getId())
                .type(conv.getType())
                .name(conv.getName())
                .description(conv.getDescription())
                .avatarUrl(conv.getAvatarUrl())
                .adminId(conv.getAdminId())
                .participants(participantResponses)
                .lastMessage(conv.getLastMessage())
                .unreadCount(unreadCount)
                .createdAt(conv.getCreatedAt())
                .updatedAt(conv.getUpdatedAt())
                .build();
    }
}
