package ma.startup.platform.pitchservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.pitchservice.client.AuthServiceClient;
import ma.startup.platform.pitchservice.client.StartupServiceClient;
import ma.startup.platform.pitchservice.dto.*;
import ma.startup.platform.pitchservice.model.PitchType;
import ma.startup.platform.pitchservice.service.AIService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour les fonctionnalités avancées d'IA
 */
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
@Slf4j
public class AIController {

    private final AIService aiService;
    private final AuthServiceClient authServiceClient;
    private final StartupServiceClient startupServiceClient;

    /**
     * Générer un Elevator Pitch (30 secondes)
     * POST /api/ai/generate-elevator
     */
    @PostMapping("/generate-elevator")
    public ResponseEntity<Map<String, String>> generateElevatorPitch(
            @Valid @RequestBody PitchRequestDTO request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Génération d'un elevator pitch");

        // Vérifier l'utilisateur et récupérer la startup
        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        // Générer le pitch
        String pitch = aiService.generatePitch(
                request.getProbleme(),
                request.getSolution(),
                request.getCible(),
                request.getAvantage(),
                startup,
                PitchType.ELEVATOR
        );

        Map<String, String> response = new HashMap<>();
        response.put("type", "ELEVATOR");
        response.put("pitch", pitch);
        response.put("startupName", startup.getNom());

        return ResponseEntity.ok(response);
    }

    /**
     * Générer une structure de Pitch Deck complète
     * POST /api/ai/generate-deck
     */
    @PostMapping("/generate-deck")
    public ResponseEntity<Map<String, String>> generatePitchDeck(
            @Valid @RequestBody PitchRequestDTO request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Génération d'une structure pitch deck");

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        String pitchDeck = aiService.generatePitch(
                request.getProbleme(),
                request.getSolution(),
                request.getCible(),
                request.getAvantage(),
                startup,
                PitchType.DECK
        );

        Map<String, String> response = new HashMap<>();
        response.put("type", "DECK");
        response.put("pitch", pitchDeck);
        response.put("startupName", startup.getNom());

        return ResponseEntity.ok(response);
    }

    /**
     * Améliorer un pitch existant
     * POST /api/ai/improve
     */
    @PostMapping("/improve")
    public ResponseEntity<Map<String, String>> improvePitch(
            @RequestBody ImprovePitchRequestDTO request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Amélioration d'un pitch existant");

        // Vérifier l'utilisateur
        UserDTO user = authServiceClient.getCurrentUser(authToken);

        String improvedPitch = aiService.improvePitch(
                request.getPitch(),
                request.getSuggestions()
        );

        Map<String, String> response = new HashMap<>();
        response.put("originalPitch", request.getPitch());
        response.put("improvedPitch", improvedPitch);
        response.put("suggestions", request.getSuggestions());

        return ResponseEntity.ok(response);
    }

    /**
     * Générer des suggestions d'amélioration
     * POST /api/ai/suggestions
     */
    @PostMapping("/suggestions")
    public ResponseEntity<Map<String, String>> generateSuggestions(
            @RequestBody AnalyzePitchRequestDTO request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Génération de suggestions pour un pitch");

        // Vérifier l'utilisateur
        UserDTO user = authServiceClient.getCurrentUser(authToken);

        String suggestions = aiService.generateSuggestions(request.getPitch());

        Map<String, String> response = new HashMap<>();
        response.put("pitch", request.getPitch());
        response.put("suggestions", suggestions);

        return ResponseEntity.ok(response);
    }

    /**
     * Test de connectivité avec l'API Hugging Face
     * GET /api/ai/test
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> testAIConnection(
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Test de connectivité avec Hugging Face");

        try {
            UserDTO user = authServiceClient.getCurrentUser(authToken);
            StartupDTO startup = startupServiceClient.getMyStartup(authToken);

            // Test simple
            String testPitch = aiService.generatePitch(
                    "Test de problème",
                    "Test de solution",
                    "Test de cible",
                    "Test d'avantage",
                    startup,
                    PitchType.ELEVATOR
            );

            Map<String, String> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Connexion Hugging Face réussie");
            response.put("testPitch", testPitch);
            response.put("pitchLength", String.valueOf(testPitch.length()));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("status", "ERROR");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}

/**
 * DTO pour l'amélioration de pitch
 */
class ImprovePitchRequestDTO {
    private String pitch;
    private String suggestions;

    public String getPitch() {
        return pitch;
    }

    public void setPitch(String pitch) {
        this.pitch = pitch;
    }

    public String getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(String suggestions) {
        this.suggestions = suggestions;
    }
}

/**
 * DTO pour l'analyse de pitch
 */
class AnalyzePitchRequestDTO {
    private String pitch;

    public String getPitch() {
        return pitch;
    }

    public void setPitch(String pitch) {
        this.pitch = pitch;
    }
}