package com.davanddev.drillbi_backend.service;

import com.davanddev.drillbi_backend.models.User;
import com.davanddev.drillbi_backend.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final List<String> adminEmails;
    private final List<String> educatorEmails;
    private final Map<String, List<String>> groupMappings;

    public CustomOAuth2UserService(
            UserRepository userRepository,
            @Value("${admin.emails:}") String adminEmailsStr,
            @Value("${educator.emails:}") String educatorEmailsStr,
            @Value("${group.mapping:{}}") String groupMappingJson
    ) {
        this.userRepository = userRepository;
        this.adminEmails = splitAndTrim(adminEmailsStr);
        this.educatorEmails = splitAndTrim(educatorEmailsStr);

        ObjectMapper mapper = new ObjectMapper();
        try {
            this.groupMappings = mapper.readValue(groupMappingJson, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse group.mapping from .env", e);
        }
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
        OAuth2User oauthUser = delegate.loadUser(userRequest);

        String email = oauthUser.getAttribute("email");
        String givenName = oauthUser.getAttribute("given_name");
        String familyName = oauthUser.getAttribute("family_name");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found in OAuth2 user attributes");
        }

        String role = determineRole(email);
        String userGroup = determineGroup(email);

        // Load or create our User entity
        User user = userRepository.findByUsername(email).orElseGet(() -> {
            User u = new User();
            u.setUsername(email);
            u.setPassword(""); // no local password yet
            u.setFirstName(givenName);
            u.setLastName(familyName);
            u.setRole(role);
            u.setUserGroup(userGroup);
            return userRepository.save(u);
        });

        // Update if role or group changed
        boolean changed = false;
        if (!Objects.equals(user.getRole(), role)) {
            user.setRole(role);
            changed = true;
        }
        if (!Objects.equals(user.getUserGroup(), userGroup)) {
            user.setUserGroup(userGroup);
            changed = true;
        }
        if (changed) {
            userRepository.save(user);
        }

        return oauthUser;
    }

    private String determineRole(String email) {
        if (adminEmails.contains(email)) {
            return "ROLE_ADMIN";
        } else if (educatorEmails.contains(email)) {
            return "ROLE_EDUCATOR";
        } else {
            return "ROLE_USER";
        }
    }

    private String determineGroup(String email) {
        for (var entry : groupMappings.entrySet()) {
            if (entry.getValue().contains(email)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private List<String> splitAndTrim(String input) {
        if (input == null || input.isBlank()) return List.of();
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}