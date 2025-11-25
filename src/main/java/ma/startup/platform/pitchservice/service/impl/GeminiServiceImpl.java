package ma.startup.platform.pitchservice.service.impl;

import com.google.api.client.util.Value;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.pitchservice.dto.GeminiRequestDTO;
import ma.startup.platform.pitchservice.dto.StartupDTO;
import ma.startup.platform.pitchservice.exception.GeminiApiException;
import ma.startup.platform.pitchservice.model.PitchType;
import ma.startup.platform.pitchservice.service.GeminiService;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.springframework.http.HttpHeaders;  // ✅ CORRECT
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Service
@Slf4j
public class GeminiServiceImpl implements GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.timeout:15000}")
    private int timeout;

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
        log.info("Génération de pitch pour startup: {} (Type: {})", startup.getNom(), type);

        String prompt = buildPrompt(probleme, solution, cible, avantage, startup, type);

        try {
            String response = callGeminiApi(prompt);
            log.info("Pitch généré avec succès pour startup: {}", startup.getNom());
            return response;

        } catch (Exception e) {
            log.error("Erreur lors de la génération du pitch: {}", e.getMessage(), e);
            throw new GeminiApiException("Impossible de générer le pitch", e);
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

    /**
     * Appel à l'API Google Gemini
     */
    private String callGeminiApi(String prompt) {
        String url = apiUrl + "?key=" + apiKey;

        // Construire la requête
        GeminiRequestDTO.Part part = GeminiRequestDTO.Part.builder()
                .text(prompt)
                .build();

        GeminiRequestDTO.Content content = GeminiRequestDTO.Content.builder()
                .parts(List.of(part))
                .build();

        GeminiRequestDTO request = GeminiRequestDTO.builder()
                .contents(List.of(content))
                .build();

        // Headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GeminiRequestDTO> entity = new HttpEntity<>(request, headers);

        try {
            // Appel API avec timeout
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return extractTextFromResponse(response.getBody());
            } else {
                throw new GeminiApiException("Réponse invalide de l'API Gemini");
            }

        } catch (RestClientException e) {
            log.error("Erreur lors de l'appel à l'API Gemini: {}", e.getMessage());
            throw new GeminiApiException("Erreur de communication avec l'API Gemini", e);
        }
    }

    /**
     * Extraire le texte de la réponse JSON de Gemini
     */
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

    /**
     * Construire le prompt pour la génération de pitch
     */
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
        prompt.append("Langue : Français professionnel");

        return prompt.toString();
    }

    /**
     * Construire le prompt pour améliorer un pitch
     */
    private String buildImprovementPrompt(String pitchExistant, String suggestions) {
        return String.format(
                "Tu es un expert en pitchs de start-ups.\n\n" +
                        "Voici un pitch existant :\n%s\n\n" +
                        "Suggestions d'amélioration :\n%s\n\n" +
                        "Améliore ce pitch en tenant compte des suggestions. " +
                        "Garde le même ton professionnel et la même longueur approximative. " +
                        "Réponds uniquement avec le pitch amélioré.",
                pitchExistant, suggestions
        );
    }

    /**
     * Construire le prompt pour les suggestions
     */
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
