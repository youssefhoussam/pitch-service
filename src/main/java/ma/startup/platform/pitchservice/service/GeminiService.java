package ma.startup.platform.pitchservice.service;

import ma.startup.platform.pitchservice.dto.StartupDTO;
import ma.startup.platform.pitchservice.model.PitchType;

public interface GeminiService {

    /**
     * Génère un pitch via l'API Google Gemini
     */
    String generatePitch(
            String probleme,
            String solution,
            String cible,
            String avantage,
            StartupDTO startup,
            PitchType type
    );

    /**
     * Améliore un pitch existant
     */
    String improvePitch(String pitchExistant, String suggestions);

    /**
     * Génère des suggestions d'amélioration
     */
    String generateSuggestions(String pitch);
}
