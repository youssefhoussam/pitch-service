package ma.startup.platform.pitchservice.controller;

import lombok.RequiredArgsConstructor;
import ma.startup.platform.pitchservice.client.AuthServiceClient;
import ma.startup.platform.pitchservice.client.StartupServiceClient;
import ma.startup.platform.pitchservice.dto.StartupDTO;
import ma.startup.platform.pitchservice.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final AuthServiceClient authServiceClient;
    private final StartupServiceClient startupServiceClient;

    @GetMapping("/test-auth")
    public ResponseEntity<Map<String, Object>> testAuth(
            @RequestHeader("Authorization") String authToken
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            UserDTO user = authServiceClient.getCurrentUser(authToken);
            response.put("success", true);
            response.put("user", user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getClass().getName());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-startup")
    public ResponseEntity<Map<String, Object>> testStartup(
            @RequestHeader("Authorization") String authToken
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            StartupDTO startup = startupServiceClient.getMyStartup(authToken);
            response.put("success", true);
            response.put("startup", startup);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("stackTrace", e.getClass().getName());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/test-both")
    public ResponseEntity<Map<String, Object>> testBoth(
            @RequestHeader("Authorization") String authToken
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Test Auth
            UserDTO user = authServiceClient.getCurrentUser(authToken);
            response.put("authSuccess", true);
            response.put("user", user);

            // Test Startup
            StartupDTO startup = startupServiceClient.getMyStartup(authToken);
            response.put("startupSuccess", true);
            response.put("startup", startup);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorClass", e.getClass().getName());

            if (e.getCause() != null) {
                response.put("cause", e.getCause().getMessage());
            }

            return ResponseEntity.status(500).body(response);
        }
    }
}
