package ma.startup.platform.pitchservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.pitchservice.dto.StartupDTO;
import ma.startup.platform.pitchservice.exception.AIException;
import ma.startup.platform.pitchservice.model.PitchType;
import ma.startup.platform.pitchservice.service.AIService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Service utilisant l'API Groq (ULTRA RAPIDE ET GRATUIT)
 * https://console.groq.com
 *
 * Avantages:
 * - Gratuit avec rate limits généreux
 * - Très rapide (inference en millisecondes)
 * - API stable et fiable
 * - Compatible OpenAI format
 */
@Service
@Slf4j
public class GroqAIService implements AIService {

    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.model:llama3-8b-8192}")
    private String model;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GroqAIService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generatePitch(
            String probleme,
            String solution,
            String cible,
            String avantage,
            StartupDTO startup,
            PitchType type
    ) {
        log.info("🚀 Génération avec Groq - Modèle: {}", model);

        String prompt = buildPrompt(probleme, solution, cible, avantage, startup, type);

        try {
            String response = callGroqAPI(prompt);
            log.info("✅ Pitch généré en <1s avec Groq");
            return cleanResponse(response);
        } catch (Exception e) {
            log.error("❌ Erreur Groq: {}", e.getMessage());
            throw new AIException("Erreur génération Groq", e);
        }
    }

    @Override
    public String improvePitch(String pitchExistant, String suggestions) {
        String prompt = String.format(
                "Améliore ce pitch selon ces suggestions:\n\nPitch: %s\n\nSuggestions: %s\n\nPitch amélioré:",
                pitchExistant, suggestions
        );

        try {
            return cleanResponse(callGroqAPI(prompt));
        } catch (Exception e) {
            throw new AIException("Erreur amélioration", e);
        }
    }

    @Override
    public String generateSuggestions(String pitch) {
        String prompt = String.format(
                "Analyse ce pitch et donne 3-5 suggestions d'amélioration:\n\n%s\n\nSuggestions:",
                pitch
        );

        try {
            return cleanResponse(callGroqAPI(prompt));
        } catch (Exception e) {
            throw new AIException("Erreur suggestions", e);
        }
    }

    private String callGroqAPI(String prompt) {
        // Format OpenAI-compatible
        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 500);
        requestBody.put("top_p", 1);
        requestBody.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GROQ_API_URL,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractResponse(response.getBody());
            }

            throw new AIException("Réponse invalide");

        } catch (Exception e) {
            log.error("Erreur API Groq: {}", e.getMessage());
            throw new AIException("Erreur communication Groq", e);
        }
    }

    private String extractResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode choices = root.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                return choices.get(0)
                        .path("message")
                        .path("content")
                        .asText();
            }

            throw new AIException("Format réponse invalide");
        } catch (Exception e) {
            throw new AIException("Parsing impossible", e);
        }
    }

    private String buildPrompt(
            String probleme,
            String solution,
            String cible,
            String avantage,
            StartupDTO startup,
            PitchType type
    ) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("Tu es un expert en pitchs de start-ups.\n\n");
        prompt.append("Génère un pitch professionnel EN FRANÇAIS pour:\n\n");
        prompt.append("Startup: ").append(startup.getNom()).append("\n");
        prompt.append("Secteur: ").append(startup.getSecteur()).append("\n");
        prompt.append("Problème: ").append(probleme).append("\n");
        prompt.append("Solution: ").append(solution).append("\n");
        prompt.append("Cible: ").append(cible).append("\n");
        prompt.append("Avantage: ").append(avantage).append("\n\n");

        switch (type) {
            case ELEVATOR:
                prompt.append("Crée un elevator pitch de 120-150 mots.\n");
                break;
            case DECK:
                prompt.append("Crée une structure pitch deck complète.\n");
                break;
            case VALUE_PROP:
                prompt.append("Crée une proposition de valeur de 80-100 mots.\n");
                break;
        }

        prompt.append("\nRéponds UNIQUEMENT avec le pitch, sans introduction.\n");
        prompt.append("Langue: FRANÇAIS\n");

        return prompt.toString();
    }

    private String cleanResponse(String response) {
        if (response == null) return "";

        String cleaned = response.trim();

        // Supprimer préfixes courants
        String[] prefixes = {"Voici", "Le pitch", "Pitch:"};
        for (String prefix : prefixes) {
            if (cleaned.startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length()).trim();
                if (cleaned.startsWith(":")) {
                    cleaned = cleaned.substring(1).trim();
                }
            }
        }

        return cleaned;
    }
}