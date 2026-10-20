package com.library.api.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/vulnerable")
public class VulnerableDemoController {

    private static final Logger log = LoggerFactory.getLogger(VulnerableDemoController.class);

    // #4 CWE-798: Hardcoded secret
    private static final String API_KEY = "sk_live_PLACEHOLDER_FOR_DEMO_PURPOSES";

    @Autowired
    private EntityManager entityManager;

    // #1 CWE-89: SQL Injection via concatenation
    @GetMapping("/search")
    public ResponseEntity<List<?>> vulnerableSearch(@RequestParam String q) {
        log.info("Searching for books with title containing: " + q);
        String sql = "SELECT * FROM book WHERE title LIKE '%" + q + "%'";
        List<?> results = entityManager.createNativeQuery(sql).getResultList();
        return ResponseEntity.ok(results);
    }

    // #2 CWE-79: XSS HTML in JSON
    @GetMapping("/greet")
    public ResponseEntity<Map<String, String>> vulnerableGreet(@RequestParam String name) {
        Map<String, String> response = new HashMap<>();
        // SonarQube might miss this because it's application/json
        String html = "<h1>Hello " + name + "</h1>";
        response.put("message", html);
        return ResponseEntity.ok(response);
    }

    // #3 CWE-22: Path Traversal
    @GetMapping("/files")
    public ResponseEntity<String> vulnerableFileRead(@RequestParam String filename) {
        try {
            Path base = Paths.get("/tmp/library-files");
            Path target = base.resolve(filename); // No validation
            String content = Files.readString(target);
            return ResponseEntity.ok(content);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error reading file: " + e.getMessage());
        }
    }

    // #5 CWE-338: Weak Random
    @GetMapping("/token")
    public ResponseEntity<Map<String, String>> weakToken() {
        Random random = new Random(); // Weak PRNG
        String token = String.valueOf(random.nextInt(999999));
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    // #6 CWE-117: Log Injection
    @GetMapping("/log")
    public ResponseEntity<String> logInjection(@RequestParam String userInput) {
        // User input directly into logs can lead to log forging
        log.info("User input: " + userInput);
        return ResponseEntity.ok("Logged: " + userInput);
    }
}
