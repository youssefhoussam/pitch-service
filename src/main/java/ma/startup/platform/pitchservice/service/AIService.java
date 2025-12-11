package ma.startup.platform.pitchservice.service;

import ma.startup.platform.pitchservice.dto.StartupDTO;
import ma.startup.platform.pitchservice.model.PitchType;

/**
 * Interface générique pour les services d'IA
 * Permet de changer facilement de provider (Gemini -> HuggingFace -> OpenAI, etc.)
 */
public interface AIService {

    /**
     * Génère un pitch professionnel basé sur les informations fournies
     *
     * @param probleme Le problème que la startup résout
     * @param solution La solution proposée
     * @param cible Le marché cible
     * @param avantage L'avantage concurrentiel
     * @param startup Les informations de la startup
     * @param type Le type de pitch à générer
     * @return Le pitch généré par l'IA
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
     * Améliore un pitch existant selon des suggestions
     *
     * @param pitchExistant Le pitch à améliorer
     * @param suggestions Les suggestions d'amélioration
     * @return Le pitch amélioré
     */
    String improvePitch(String pitchExistant, String suggestions);

    /**
     * Génère des suggestions d'amélioration pour un pitch
     *
     * @param pitch Le pitch à analyser
     * @return Les suggestions d'amélioration
     */
    String generateSuggestions(String pitch);
}
