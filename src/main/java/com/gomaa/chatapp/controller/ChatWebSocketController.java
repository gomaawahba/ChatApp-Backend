package com.gomaa.chatapp.controller;

import com.gomaa.chatapp.dto.response.WebSocketEvent;
import com.gomaa.chatapp.service.PresenceService;
import com.gomaa.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PresenceService presenceService;
    private final UserService userService;

    /**
     * Client sends: /app/typing
     * Broadcasts typing indicator to conversation topic
     */
    @MessageMapping("/typing")
    public void handleTyping(@Payload Map<String, String> payload, Principal principal) {
        String conversationId = payload.get("conversationId");
        boolean isTyping = Boolean.parseBoolean(payload.getOrDefault("typing", "true"));

        WebSocketEvent.EventType eventType = isTyping
                ? WebSocketEvent.EventType.USER_TYPING
                : WebSocketEvent.EventType.USER_STOP_TYPING;

        WebSocketEvent event = WebSocketEvent.of(eventType, Map.of(
                "username", principal.getName(),
                "conversationId", conversationId
        ));

        messagingTemplate.convertAndSend("/topic/conversation." + conversationId, event);
    }

    /**
     * Fired when a WebSocket session is established
     */
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String username = accessor.getUser().getName();
            try {
                String userId = userService.getByUsername(username).getId();
                presenceService.markOnline(userId);

                // Broadcast online status
                WebSocketEvent onlineEvent = WebSocketEvent.of(
                        WebSocketEvent.EventType.USER_ONLINE, Map.of("username", username, "userId", userId));
                messagingTemplate.convertAndSend("/topic/presence", onlineEvent);

                log.info("WS connected: {}", username);
            } catch (Exception e) {
                log.warn("Could not resolve user on connect: {}", username);
            }
        }
    }

    /**
     * Fired when a WebSocket session is disconnected
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        if (accessor.getUser() != null) {
            String username = accessor.getUser().getName();
            try {
                String userId = userService.getByUsername(username).getId();
                presenceService.markOffline(userId);

                WebSocketEvent offlineEvent = WebSocketEvent.of(
                        WebSocketEvent.EventType.USER_OFFLINE, Map.of("username", username, "userId", userId));
                messagingTemplate.convertAndSend("/topic/presence", offlineEvent);

                log.info("WS disconnected: {}", username);
            } catch (Exception e) {
                log.warn("Could not resolve user on disconnect: {}", username);
            }
        }
    }
}
