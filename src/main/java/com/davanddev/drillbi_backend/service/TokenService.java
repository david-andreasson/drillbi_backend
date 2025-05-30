package com.davanddev.drillbi_backend.service;

import com.davanddev.drillbi_backend.models.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class TokenService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public String generateToken(User user) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        long expirationMillis = 1000 * 60 * 60 * 24;

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .claim("firstName", user.getFirstName())

                .claim("userGroup", user.getUserGroup())
                .claim("email", user.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
