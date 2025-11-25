package ma.startup.platform.pitchservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    @Value("${spring.application.name}")
    private String serviceName;

    @Value("${server.port}")
    private String port;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("service", serviceName);
        health.put("status", "UP");
        health.put("port", port);
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/ready")
    public ResponseEntity<String> readiness() {
        return ResponseEntity.ok("Service is ready");
    }

    @GetMapping("/live")
    public ResponseEntity<String> liveness() {
        return ResponseEntity.ok("Service is alive");
    }
}
