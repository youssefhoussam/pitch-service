package ma.startup.platform.pitchservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.pitchservice.client.AuthServiceClient;
import ma.startup.platform.pitchservice.client.StartupServiceClient;
import ma.startup.platform.pitchservice.dto.*;
import ma.startup.platform.pitchservice.model.Pitch;
import ma.startup.platform.pitchservice.model.PitchType;
import ma.startup.platform.pitchservice.repository.PitchRepository;
import ma.startup.platform.pitchservice.service.PitchService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pitchs")
@RequiredArgsConstructor
@Slf4j
public class PitchController {

    private final PitchService pitchService;
    private final AuthServiceClient authServiceClient;
    private final StartupServiceClient startupServiceClient;
    private final PitchRepository pitchRepository;

    /**
     * ENDPOINT PRINCIPAL : Générer un nouveau pitch avec l'IA
     * POST /api/pitchs/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<PitchResponseDTO> generatePitch(
            @Valid @RequestBody PitchRequestDTO request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Requête de génération de pitch reçue");
        PitchResponseDTO response = pitchService.generatePitch(request, authToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer tous mes pitchs
     * GET /api/pitchs/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<PitchResponseDTO>> getMyPitchs(
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Récupération de tous les pitchs");
        List<PitchResponseDTO> pitchs = pitchService.getMyPitchs(authToken);
        return ResponseEntity.ok(pitchs);
    }

    /**
     * Récupérer mes pitchs avec pagination
     * GET /api/pitchs/me/paginated?page=0&size=10
     */
    @GetMapping("/me/paginated")
    public ResponseEntity<Page<PitchResponseDTO>> getMyPitchsPaginated(
            @RequestHeader("Authorization") String authToken,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction
    ) {
        log.info("Récupération des pitchs paginés - page: {}, size: {}", page, size);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<PitchResponseDTO> pitchsPage = pitchService.getMyPitchsPaginated(authToken, pageable);

        return ResponseEntity.ok(pitchsPage);
    }

    /**
     * Récupérer un pitch spécifique par ID
     * GET /api/pitchs/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PitchResponseDTO> getPitchById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Récupération du pitch ID: {}", id);
        PitchResponseDTO pitch = pitchService.getPitchById(id, authToken);
        return ResponseEntity.ok(pitch);
    }

    /**
     * Modifier un pitch existant
     * PUT /api/pitchs/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PitchResponseDTO> updatePitch(
            @PathVariable UUID id,
            @Valid @RequestBody PitchRequestDTO request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Modification du pitch ID: {}", id);
        PitchResponseDTO updatedPitch = pitchService.updatePitch(id, request, authToken);
        return ResponseEntity.ok(updatedPitch);
    }

    /**
     * Supprimer un pitch
     * DELETE /api/pitchs/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePitch(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Suppression du pitch ID: {}", id);
        pitchService.deletePitch(id, authToken);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle favori sur un pitch
     * PATCH /api/pitchs/{id}/favorite
     */
    @PatchMapping("/{id}/favorite")
    public ResponseEntity<PitchResponseDTO> toggleFavorite(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Toggle favori pour pitch ID: {}", id);
        PitchResponseDTO updatedPitch = pitchService.toggleFavorite(id, authToken);
        return ResponseEntity.ok(updatedPitch);
    }

    /**
     * Noter un pitch (1-5 étoiles)
     * POST /api/pitchs/{id}/rate
     */
    @PostMapping("/{id}/rate")
    public ResponseEntity<PitchResponseDTO> ratePitch(
            @PathVariable UUID id,
            @RequestParam Integer rating,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Notation du pitch ID: {} avec note: {}", id, rating);
        PitchResponseDTO ratedPitch = pitchService.ratePitch(id, rating, authToken);
        return ResponseEntity.ok(ratedPitch);
    }

    /**
     * Récupérer les pitchs favoris uniquement
     * GET /api/pitchs/me/favorites
     */
    @GetMapping("/me/favorites")
    public ResponseEntity<List<PitchResponseDTO>> getFavoritePitchs(
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Récupération des pitchs favoris");
        List<PitchResponseDTO> favoritePitchs = pitchService.getFavoritePitchs(authToken);
        return ResponseEntity.ok(favoritePitchs);
    }

    /**
     * Récupérer les statistiques des pitchs
     * GET /api/pitchs/me/stats
     */
    @GetMapping("/me/stats")
    public ResponseEntity<PitchStatsDTO> getMyPitchStats(
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Récupération des statistiques des pitchs");
        PitchStatsDTO stats = pitchService.getMyPitchStats(authToken);
        return ResponseEntity.ok(stats);
    }
    @PostMapping("/generate-test")
    public ResponseEntity<PitchResponseDTO> generateTestPitch(
            @Valid @RequestBody PitchRequestDTO request,
            @RequestHeader("Authorization") String authToken
    ) {
        log.info("Test de génération de pitch SANS Gemini");

        // Vérifier l'utilisateur
        UserDTO user = authServiceClient.getCurrentUser(authToken);

        // Récupérer la startup
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        // Créer un pitch de test SANS appeler Gemini
        Pitch pitch = Pitch.builder()
                .startupId(startup.getId())
                .probleme(request.getProbleme())
                .solution(request.getSolution())
                .cible(request.getCible())
                .avantage(request.getAvantage())
                .pitchGenere("PITCH DE TEST : " + startup.getNom() + " résout " + request.getProbleme() + " avec " + request.getSolution())
                .type(PitchType.ELEVATOR)
                .isFavorite(false)
                .build();

        Pitch savedPitch = pitchRepository.save(pitch);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponseDTO(savedPitch));
    }

    private PitchResponseDTO mapToResponseDTO(Pitch pitch) {
        return PitchResponseDTO.builder()
                .id(pitch.getId())
                .startupId(pitch.getStartupId())
                .probleme(pitch.getProbleme())
                .solution(pitch.getSolution())
                .cible(pitch.getCible())
                .avantage(pitch.getAvantage())
                .pitchGenere(pitch.getPitchGenere())
                .type(pitch.getType())
                .rating(pitch.getRating())
                .isFavorite(pitch.getIsFavorite())
                .createdAt(pitch.getCreatedAt())
                .updatedAt(pitch.getUpdatedAt())
                .build();
    }
}
