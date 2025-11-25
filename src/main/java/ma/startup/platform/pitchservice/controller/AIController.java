package ma.startup.platform.pitchservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.pitchservice.client.AuthServiceClient;
import ma.startup.platform.pitchservice.client.StartupServiceClient;
import ma.startup.platform.pitchservice.dto.StartupDTO;
import ma.startup.platform.pitchservice.dto.UserDTO;
import ma.startup.platform.pitchservice.model.PitchType;
import ma.startup.platform.pitchservice.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final GeminiService geminiService;
    private final AuthServiceClient authServiceClient;
    private final StartupServiceClient startupServiceClient;

    /**
     * Générer un elevator pitch (30 secondes)
     * POST /api/ai/generate-elevator
     */
    @PostMapping("/generate-elevator")
    public ResponseEntity<Map<String, String>> generateElevatorPitch(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Génération d'un elevator pitch");

        // Vérifier l'utilisateur
        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        // Générer le pitch
        String pitch = geminiService.generatePitch(
                request.get("probleme"),
                request.get("solution"),
                request.get("cible"),
                request.get("avantage"),
                startup,
                PitchType.ELEVATOR
        );

        Map<String, String> response = new HashMap<>();
        response.put("pitch", pitch);
        response.put("type", "ELEVATOR");

        return ResponseEntity.ok(response);
    }

    /**
     * Générer une structure de pitch deck
     * POST /api/ai/generate-deck
     */
    @PostMapping("/generate-deck")
    public ResponseEntity<Map<String, String>> generateDeckPitch(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Génération d'un pitch deck");

        // Vérifier l'utilisateur
        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        // Générer le pitch deck
        String pitchDeck = geminiService.generatePitch(
                request.get("probleme"),
                request.get("solution"),
                request.get("cible"),
                request.get("avantage"),
                startup,
                PitchType.DECK
        );

        Map<String, String> response = new HashMap<>();
        response.put("pitch", pitchDeck);
        response.put("type", "DECK");

        return ResponseEntity.ok(response);
    }

    /**
     * Améliorer un pitch existant
     * POST /api/ai/improve
     */
    @PostMapping("/improve")
    public ResponseEntity<Map<String, String>> improvePitch(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Amélioration d'un pitch existant");

        // Vérifier l'utilisateur
        UserDTO user = authServiceClient.getCurrentUser(authToken);

        String pitchExistant = request.get("pitch");
        String suggestions = request.getOrDefault("suggestions", "Rends ce pitch plus percutant et professionnel");

        // Améliorer le pitch
        String improvedPitch = geminiService.improvePitch(pitchExistant, suggestions);

        Map<String, String> response = new HashMap<>();
        response.put("originalPitch", pitchExistant);
        response.put("improvedPitch", improvedPitch);

        return ResponseEntity.ok(response);
    }

    /**
     * Générer des suggestions d'amélioration
     * POST /api/ai/suggestions
     */
    @PostMapping("/suggestions")
    public ResponseEntity<Map<String, String>> generateSuggestions(
            @RequestBody Map<String, String> request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Génération de suggestions d'amélioration");

        // Vérifier l'utilisateur
        UserDTO user = authServiceClient.getCurrentUser(authToken);

        String pitch = request.get("pitch");

        // Générer les suggestions
        String suggestions = geminiService.generateSuggestions(pitch);

        Map<String, String> response = new HashMap<>();
        response.put("pitch", pitch);
        response.put("suggestions", suggestions);

        return ResponseEntity.ok(response);
    }

    /**
     * Test de l'API Gemini
     * GET /api/ai/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testGeminiApi(
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Test de l'API Gemini");

        try {
            // Vérifier l'utilisateur
            UserDTO user = authServiceClient.getCurrentUser(authToken);
            StartupDTO startup = startupServiceClient.getMyStartup(authToken);

            // Test simple
            String testPitch = geminiService.generatePitch(
                    "Les entrepreneurs perdent du temps à créer des pitchs",
                    "Une plateforme IA qui génère des pitchs en 2 minutes",
                    "Start-ups marocaines",
                    "Génération automatique et professionnelle",
                    startup,
                    PitchType.ELEVATOR
            );

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "API Gemini fonctionne correctement");
            response.put("samplePitch", testPitch);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors du test de l'API Gemini: {}", e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Erreur: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}

