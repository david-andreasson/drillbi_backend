package com.davanddev.quizapp_api.controller;

import com.davanddev.quizapp_api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/users")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", jwt.getSubject());
        userInfo.put("firstName", jwt.getClaimAsString("firstName"));
        userInfo.put("lastName", jwt.getClaimAsString("lastName"));
        userInfo.put("role", jwt.getClaimAsString("role"));
        userInfo.put("userGroup", jwt.getClaimAsString("userGroup"));

        return ResponseEntity.ok(userInfo);
    }
}