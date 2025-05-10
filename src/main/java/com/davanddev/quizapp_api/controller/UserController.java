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

        // Get user from database to get premium status and Stripe ID
        var userOpt = userRepository.findByUsername(jwt.getSubject());
        if (userOpt.isPresent()) {
            var user = userOpt.get();
            userInfo.put("isPremium", user.isPremium());
            userInfo.put("stripeCustomerId", user.getStripeCustomerId());
            userInfo.put("stripeSubscriptionId", user.getStripeSubscriptionId());
        } else {
            userInfo.put("isPremium", false);
            userInfo.put("stripeCustomerId", null);
            userInfo.put("stripeSubscriptionId", null);
        }

        return ResponseEntity.ok(userInfo);
    }

    @PatchMapping("/me")
    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, String> updates) {
        var userOpt = userRepository.findByUsername(jwt.getSubject());
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var user = userOpt.get();
        // Uppdatera förnamn
        if (updates.containsKey("firstName")) {
            user.setFirstName(updates.get("firstName"));
        }
        // Uppdatera efternamn
        if (updates.containsKey("lastName")) {
            user.setLastName(updates.get("lastName"));
        }
        // Uppdatera användarnamn (om det är nytt och inte upptaget)
        if (updates.containsKey("username")) {
            String newUsername = updates.get("username");
            if (!newUsername.equals(user.getUsername())) {
                if (userRepository.findByUsername(newUsername).isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Användarnamnet är redan upptaget."));
                }
                user.setUsername(newUsername);
            }
        }
        // Uppdatera e-post (om det är nytt och inte upptaget)
        if (updates.containsKey("email")) {
            String newEmail = updates.get("email");
            if (!newEmail.equals(user.getEmail())) {
                if (userRepository.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "E-postadressen är redan upptagen."));
                }
                user.setEmail(newEmail);
            }
        }
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}