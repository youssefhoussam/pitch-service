package ma.startup.platform.pitchservice.service;

import ma.startup.platform.pitchservice.dto.PitchRequestDTO;
import ma.startup.platform.pitchservice.dto.PitchResponseDTO;
import ma.startup.platform.pitchservice.dto.PitchStatsDTO;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface PitchService {

    /**
     * Générer un nouveau pitch avec l'IA
     */
    PitchResponseDTO generatePitch(PitchRequestDTO request, String authToken);

    /**
     * Récupérer tous les pitchs d'un utilisateur
     */
    List<PitchResponseDTO> getMyPitchs(String authToken);

    /**
     * Récupérer un pitch spécifique
     */
    PitchResponseDTO getPitchById(UUID pitchId, String authToken);

    /**
     * Modifier un pitch manuellement
     */
    PitchResponseDTO updatePitch(UUID pitchId, PitchRequestDTO request, String authToken);

    /**
     * Supprimer un pitch
     */
    void deletePitch(UUID pitchId, String authToken);

    /**
     * Toggle favori
     */
    PitchResponseDTO toggleFavorite(UUID pitchId, String authToken);

    /**
     * Noter un pitch
     */
    PitchResponseDTO ratePitch(UUID pitchId, Integer rating, String authToken);

    /**
     * Récupérer les pitchs favoris
     */
    List<PitchResponseDTO> getFavoritePitchs(String authToken);

    /**
     * Récupérer les pitchs avec pagination
     */
    Page<PitchResponseDTO> getMyPitchsPaginated(String authToken, Pageable pageable);

    /**
     * Statistiques des pitchs
     */
    PitchStatsDTO getMyPitchStats(String authToken);
}
