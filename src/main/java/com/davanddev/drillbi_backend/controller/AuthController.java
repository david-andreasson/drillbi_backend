package com.davanddev.drillbi_backend.controller;

import com.davanddev.drillbi_backend.dto.AuthRequest;
import com.davanddev.drillbi_backend.models.User;
import com.davanddev.drillbi_backend.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v2/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthController(AuthenticationManager authenticationManager,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        logger.info("Attempting authentication for user {}", authRequest.getUsername());
        try {
            // 1. Authenticate
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );
            logger.info("Authentication successful for user {}", authRequest.getUsername());
        } catch (Exception ex) {
            logger.warn("Authentication failed for user {}: {}", authRequest.getUsername(), ex.getMessage());
            throw ex;
        }

        // 2. Fetch user details
        User user = userRepository.findByUsername(authRequest.getUsername())
                .orElseThrow(() -> {
                    logger.warn("User not found in database: {}", authRequest.getUsername());
                    return new UsernameNotFoundException("User not found");
                });

        // 3. Create JWT with extra claims
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        long expirationMillis = 1000 * 60 * 60 * 24; // 24 hours

        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .claim("firstName", user.getFirstName())

                .claim("userGroup", user.getUserGroup())
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        logger.info("JWT token created with claims for user {}", user.getUsername());

        // 4. Return token
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler({ org.springframework.security.authentication.BadCredentialsException.class, UsernameNotFoundException.class })
    public ResponseEntity<?> handleAuthExceptions(Exception ex) {
        logger.warn("Authentication error: {}", ex.getMessage());
        return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                .body(java.util.Map.of("error", "Felaktigt användarnamn eller lösenord"));
    }
}
