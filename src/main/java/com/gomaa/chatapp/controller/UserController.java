package com.gomaa.chatapp.controller;


import com.gomaa.chatapp.dto.response.UserResponse;
import com.gomaa.chatapp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getByUsername(userDetails.getUsername()));
    }

    @PatchMapping("/me")
    public ResponseEntity<?> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> body) {

        // التأكد إن المستخدم موجود
        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("status", 401, "message", "Unauthorized"));
        }

        try {
            UserResponse me = userService.getByUsername(userDetails.getUsername());

            // فحص القيم قبل التحديث
            String displayName = body.get("displayName");
            String avatarUrl = body.get("avatarUrl");

            if ((displayName == null || displayName.isBlank()) &&
                    (avatarUrl == null || avatarUrl.isBlank())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("status", 400, "message", "No valid fields to update"));
            }

            UserResponse updatedUser = userService.updateProfile(me.getId(), displayName, avatarUrl);

            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            // طباعة الاستثناء للـ logs
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("status", 500, "message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        return ResponseEntity.ok(userService.getById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String q) {
        return ResponseEntity.ok(userService.searchUsers(q));
    }
}