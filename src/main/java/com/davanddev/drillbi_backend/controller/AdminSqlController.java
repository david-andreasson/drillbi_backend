package com.davanddev.drillbi_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/sql")
public class AdminSqlController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Endast ADMIN får köra denna endpoint
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> executeSql(@RequestBody SqlRequest sqlRequest) {
        String sql = sqlRequest.getSql();
        try {
            if (sql.trim().toLowerCase().startsWith("select")) {
                List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
                return ResponseEntity.ok(result);
            } else {
                int rows = jdbcTemplate.update(sql);
                return ResponseEntity.ok("Antal påverkade rader: " + rows);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Fel: " + e.getMessage());
        }
    }

    public static class SqlRequest {
        private String sql;
        public String getSql() { return sql; }
        public void setSql(String sql) { this.sql = sql; }
    }
}
