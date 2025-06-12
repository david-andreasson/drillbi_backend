package com.davanddev.drillbi_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
public class ApiFallbackController {
    // Fångar alla okända API-anrop under /api/** och returnerar JSON 404
    @RequestMapping(value = "/api/**", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH })
    public ResponseEntity<?> apiNotFound() {
        return ResponseEntity.status(404).body("{\"error\":\"API endpoint not found\"}");
    }
}
