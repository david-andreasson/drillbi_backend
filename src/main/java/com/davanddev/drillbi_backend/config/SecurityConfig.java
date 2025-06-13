package com.davanddev.drillbi_backend.config;

import com.davanddev.drillbi_backend.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Security configuration for username/password authentication, JWT resource server,
 * and H2 console access.
 */
@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final com.davanddev.drillbi_backend.security.CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler;
    private final com.davanddev.drillbi_backend.service.CustomOAuth2UserService customOAuth2UserService;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          com.davanddev.drillbi_backend.security.CustomOAuth2AuthenticationSuccessHandler customOAuth2AuthenticationSuccessHandler,
                          com.davanddev.drillbi_backend.service.CustomOAuth2UserService customOAuth2UserService) {
        this.userDetailsService = userDetailsService;
        this.customOAuth2AuthenticationSuccessHandler = customOAuth2AuthenticationSuccessHandler;
        this.customOAuth2UserService = customOAuth2UserService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Allow frames (H2 console)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                // Disable CSRF (JWT is stateless)
                .csrf(AbstractHttpConfigurer::disable)
                // Stateless session management
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Apply our authentication provider
                .authenticationProvider(authenticationProvider())
                // CORS configuration
                .cors(Customizer.withDefaults())
                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // Tillåt H2 Console
                        .requestMatchers("/h2-console/**").permitAll()
                        // Tillåt bilder publikt
                        .requestMatchers("/images/**").permitAll()
                        // Preflight
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Authentication endpoint
                        .requestMatchers("/api/v2/auth/**").permitAll()
                        // Tillåt ping-endpoint
                        .requestMatchers("/api/ping").permitAll()
                        // Tillåt OAuth2 endpoints
                        .requestMatchers("/oauth2/**", "/login/**").permitAll()
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                // OAuth2 login för Google
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(customOAuth2AuthenticationSuccessHandler)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )
                // Basic auth för token endpoint
                .httpBasic(Customizer.withDefaults())
                // JWT resource server för API-skydd
                .oauth2ResourceServer(rs -> rs.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${jwt.secret}") String jwtSecret) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, "HMACSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
