package com.davanddev.drillbi_backend.security;

import com.davanddev.drillbi_backend.models.User;
import com.davanddev.drillbi_backend.repository.UserRepository;
import com.davanddev.drillbi_backend.service.TokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
        System.out.println("[OAuth2 Success Handler] Försöker logga in användare med e-post: " + email);
        try {
            User user = userRepository.findByUsername(email).orElse(null);
            if (user == null) {
                OAuth2User principal = oauthToken.getPrincipal();
                String givenName = principal.getAttribute("given_name");
                String familyName = principal.getAttribute("family_name");
                String userGroup = null;
                if (principal.getAttributes().containsKey("userGroup")) {
                    userGroup = principal.getAttribute("userGroup");
                }
                user = new User();
                user.setUsername(email);
                user.setEmail(email);
                user.setPassword("GOOGLE-OAUTH2-NO-PASSWORD"); // Placeholder, hanteras ej för Google-login
                user.setRole("ROLE_USER");
                user.setPremium(false);
                user.setFirstName(givenName);
                user.setLastName(familyName);
                user.setUserGroup(userGroup);
                user.setStripeCustomerId(null);
                user.setStripeSubscriptionId(null);
                user = userRepository.save(user);
                System.out.println("[OAuth2 Success Handler] Ny användare skapad: " + email +
                        ", förnamn: " + givenName +
                        ", efternamn: " + familyName +
                        ", userGroup: " + userGroup);
            } else {
                System.out.println("[OAuth2 Success Handler] Befintlig användare hittad: " + email);
            }
            String token = tokenService.generateToken(user);
            System.out.println("[OAuth2 Success Handler] Token genererad och redirect till frontend sker för: " + email);
            System.out.println("[OAuth2 Success Handler] Redirect-URL: " + frontendRedirectUrl + "?token=" + token);
            response.sendRedirect(frontendRedirectUrl + "?token=" + token);
        } catch (Exception e) {
            System.err.println("[OAuth2 Success Handler] FEL vid login för: " + email);
            e.printStackTrace();
            throw e;
        }
    }
}