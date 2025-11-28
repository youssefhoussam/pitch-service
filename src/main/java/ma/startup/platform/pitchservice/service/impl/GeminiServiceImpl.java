package ma.startup.platform.pitchservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.pitchservice.dto.StartupDTO;
import ma.startup.platform.pitchservice.exception.GeminiApiException;
import ma.startup.platform.pitchservice.model.PitchType;
import ma.startup.platform.pitchservice.service.GeminiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeminiServiceImpl() {
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
        log.info("🤖 Génération de pitch avec Gemini 2.0 Flash pour: {} (Type: {})", startup.getNom(), type);

        String prompt = buildPrompt(probleme, solution, cible, avantage, startup, type);

        try {
            String response = callGeminiApi(prompt);
            log.info("✅ Pitch généré avec succès par Gemini");
            return response;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération du pitch: {}", e.getMessage(), e);
            throw new GeminiApiException("Impossible de générer le pitch avec Gemini", e);
        }
    }

    @Override
    public String improvePitch(String pitchExistant, String suggestions) {
        String prompt = buildImprovementPrompt(pitchExistant, suggestions);

        try {
            return callGeminiApi(prompt);
        } catch (Exception e) {
            log.error("Erreur lors de l'amélioration du pitch: {}", e.getMessage());
            throw new GeminiApiException("Impossible d'améliorer le pitch", e);
        }
    }

    @Override
    public String generateSuggestions(String pitch) {
        String prompt = buildSuggestionsPrompt(pitch);

        try {
            return callGeminiApi(prompt);
        } catch (Exception e) {
            log.error("Erreur lors de la génération des suggestions: {}", e.getMessage());
            throw new GeminiApiException("Impossible de générer les suggestions", e);
        }
    }

    private String callGeminiApi(String prompt) {
        log.info("🔗 Appel à l'API Gemini 2.0 Flash...");

        Map<String, Object> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-goog-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("✅ Réponse Gemini reçue avec succès");
                return extractTextFromResponse(response.getBody());
            } else {
                throw new GeminiApiException("Réponse invalide de l'API Gemini");
            }

        } catch (RestClientException e) {
            log.error("❌ Erreur lors de l'appel à l'API Gemini: {}", e.getMessage());
            throw new GeminiApiException("Erreur de communication avec l'API Gemini", e);
        }
    }

    private String extractTextFromResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode candidates = root.path("candidates");

            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.path("content");
                JsonNode parts = content.path("parts");

                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText();
                }
            }

            throw new GeminiApiException("Format de réponse invalide");

        } catch (Exception e) {
            log.error("Erreur lors du parsing de la réponse Gemini: {}", e.getMessage());
            throw new GeminiApiException("Impossible de parser la réponse de Gemini", e);
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

        prompt.append("Tu es un expert en pitchs de start-ups et en levées de fonds.\n\n");

        prompt.append("Contexte de la start-up :\n");
        prompt.append("- Nom : ").append(startup.getNom()).append("\n");
        prompt.append("- Secteur : ").append(startup.getSecteur()).append("\n");

        if (startup.getDescription() != null && !startup.getDescription().isEmpty()) {
            prompt.append("- Description : ").append(startup.getDescription()).append("\n");
        }

        prompt.append("\nInformations fournies :\n");
        prompt.append("- Problème : ").append(probleme).append("\n");
        prompt.append("- Solution : ").append(solution).append("\n");
        prompt.append("- Cible : ").append(cible).append("\n");
        prompt.append("- Avantage compétitif : ").append(avantage).append("\n\n");

        switch (type) {
            case ELEVATOR:
                prompt.append("Génère un elevator pitch professionnel de 120-150 mots maximum qui :\n");
                prompt.append("1. Accroche dès la première phrase\n");
                prompt.append("2. Présente clairement le problème et la solution\n");
                prompt.append("3. Met en avant la proposition de valeur unique\n");
                prompt.append("4. Est orienté bénéfices pour les clients\n");
                prompt.append("5. Se termine par un call-to-action implicite\n\n");
                break;

            case DECK:
                prompt.append("Génère une structure de pitch deck professionnelle avec :\n");
                prompt.append("1. Un titre accrocheur\n");
                prompt.append("2. Le problème (2-3 phrases)\n");
                prompt.append("3. La solution (2-3 phrases)\n");
                prompt.append("4. Le marché cible\n");
                prompt.append("5. L'avantage concurrentiel\n");
                prompt.append("6. Un appel à l'action\n\n");
                break;

            case VALUE_PROP:
                prompt.append("Génère une proposition de valeur claire et concise (80-100 mots) qui :\n");
                prompt.append("1. Identifie le bénéfice principal\n");
                prompt.append("2. Explique comment la solution apporte ce bénéfice\n");
                prompt.append("3. Différencie de la concurrence\n\n");
                break;
        }

        prompt.append("Ton : Professionnel, confiant, concis\n");
        prompt.append("Format : Un ou plusieurs paragraphes fluides sans bullet points\n");
        prompt.append("Langue : Français professionnel\n\n");
        prompt.append("Réponds UNIQUEMENT avec le pitch, sans introduction ni commentaire.");

        return prompt.toString();
    }

    private String buildImprovementPrompt(String pitchExistant, String suggestions) {
        return String.format(
                "Tu es un expert en pitchs de start-ups.\n\n" +
                        "Voici un pitch existant :\n%s\n\n" +
                        "Suggestions d'amélioration :\n%s\n\n" +
                        "Améliore ce pitch en tenant compte des suggestions. " +
                        "Garde le même ton professionnel et la même longueur approximative. " +
                        "Réponds uniquement avec le pitch amélioré, sans commentaire.",
                pitchExistant, suggestions
        );
    }

    private String buildSuggestionsPrompt(String pitch) {
        return String.format(
                "Tu es un expert en pitchs de start-ups.\n\n" +
                        "Analyse ce pitch :\n%s\n\n" +
                        "Fournis 3 à 5 suggestions concrètes d'amélioration concernant :\n" +
                        "- La clarté du message\n" +
                        "- L'impact des mots utilisés\n" +
                        "- La structure narrative\n" +
                        "- L'appel à l'action\n\n" +
                        "Sois concis et actionnable.",
                pitch
        );
    }
}