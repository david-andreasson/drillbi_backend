package com.davanddev.quizapp_api.security;

import com.davanddev.quizapp_api.models.User;
import com.davanddev.quizapp_api.repository.UserRepository;
import com.davanddev.quizapp_api.service.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomOAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final String frontendRedirectUrl;

    public CustomOAuth2AuthenticationSuccessHandler(
            TokenService tokenService,
            UserRepository userRepository,
            @Value("${frontend.redirect.url}") String frontendRedirectUrl) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.frontendRedirectUrl = frontendRedirectUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String email = oauthToken.getPrincipal().getAttribute("email");

        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        String token = tokenService.generateToken(user);

        response.sendRedirect(frontendRedirectUrl + "?token=" + token);
    }
}