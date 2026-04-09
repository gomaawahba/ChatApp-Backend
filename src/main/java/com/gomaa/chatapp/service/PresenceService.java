package com.gomaa.chatapp.service;



import com.gomaa.chatapp.model.UserStatus;
import com.gomaa.chatapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private static final String ONLINE_KEY_PREFIX = "user:online:";
    private static final long ONLINE_TTL_SECONDS = 300; // 5 minutes

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;

    public void markOnline(String userId) {
        String key = ONLINE_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, Instant.now().toString(), ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
        updateUserStatus(userId, UserStatus.ONLINE);
        log.debug("User {} is now ONLINE", userId);
    }

    public void markOffline(String userId) {
        String key = ONLINE_KEY_PREFIX + userId;
        redisTemplate.delete(key);
        updateUserStatus(userId, UserStatus.OFFLINE);
        log.debug("User {} is now OFFLINE", userId);
    }

    public boolean isOnline(String userId) {
        String key = ONLINE_KEY_PREFIX + userId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void refreshPresence(String userId) {
        String key = ONLINE_KEY_PREFIX + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, ONLINE_TTL_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void updateUserStatus(String userId,UserStatus status) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(status);
            user.setLastSeen(Instant.now());
            userRepository.save(user);
        });
    }
}

