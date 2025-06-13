package com.davanddev.drillbi_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/version")
public class VersionController {
    @GetMapping
    public ResponseEntity<String> getVersion() {
        try {
            String version = new String(Files.readAllBytes(Paths.get("BACKEND_VERSION.txt")));
            return ResponseEntity.ok(version.trim());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("unknown");
        }
    }
}
