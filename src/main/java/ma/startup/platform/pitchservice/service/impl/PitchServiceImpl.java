package ma.startup.platform.pitchservice.service.impl;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.pitchservice.client.AuthServiceClient;
import ma.startup.platform.pitchservice.client.StartupServiceClient;
import ma.startup.platform.pitchservice.dto.*;
import ma.startup.platform.pitchservice.exception.PitchNotFoundException;
import ma.startup.platform.pitchservice.model.Pitch;
import ma.startup.platform.pitchservice.model.PitchType;
import ma.startup.platform.pitchservice.repository.PitchRepository;
import ma.startup.platform.pitchservice.service.AIService;
import ma.startup.platform.pitchservice.service.PitchService;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PitchServiceImpl implements PitchService {

    private final PitchRepository pitchRepository;
    private final AIService aiService; // ✅ CHANGEMENT: Utilise l'interface générique
    private final AuthServiceClient authServiceClient;
    private final StartupServiceClient startupServiceClient;

    @Override
    public PitchResponseDTO generatePitch(PitchRequestDTO request, String authToken) {
        log.info("Début de génération de pitch avec Hugging Face");

        // 1. Vérifier l'utilisateur via Auth-Service
        UserDTO user = authServiceClient.getCurrentUser(authToken);
        log.info("Utilisateur vérifié: {}", user.getEmail());

        // 2. Récupérer la startup via Startup-Service
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);
        log.info("Startup récupérée: {} (ID: {})", startup.getNom(), startup.getId());

        // 3. Générer le pitch avec Hugging Face (AI Service)
        String pitchGenere = aiService.generatePitch(
                request.getProbleme(),
                request.getSolution(),
                request.getCible(),
                request.getAvantage(),
                startup,
                PitchType.ELEVATOR
        );
        log.info("Pitch généré avec succès - Longueur: {} caractères", pitchGenere.length());

        // 4. Sauvegarder en base de données
        Pitch pitch = Pitch.builder()
                .startupId(startup.getId())
                .probleme(request.getProbleme())
                .solution(request.getSolution())
                .cible(request.getCible())
                .avantage(request.getAvantage())
                .pitchGenere(pitchGenere)
                .type(PitchType.ELEVATOR)
                .isFavorite(false)
                .build();

        Pitch savedPitch = pitchRepository.save(pitch);
        log.info("Pitch sauvegardé avec ID: {}", savedPitch.getId());

        return mapToResponseDTO(savedPitch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PitchResponseDTO> getMyPitchs(String authToken) {
        log.info("Récupération des pitchs");

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        List<Pitch> pitchs = pitchRepository.findByStartupIdOrderByCreatedAtDesc(startup.getId());
        log.info("Nombre de pitchs trouvés: {}", pitchs.size());

        return pitchs.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PitchResponseDTO getPitchById(UUID pitchId, String authToken) {
        log.info("Récupération du pitch ID: {}", pitchId);

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        Pitch pitch = pitchRepository.findByIdAndStartupId(pitchId, startup.getId())
                .orElseThrow(() -> new PitchNotFoundException(pitchId));

        return mapToResponseDTO(pitch);
    }

    @Override
    public PitchResponseDTO updatePitch(UUID pitchId, PitchRequestDTO request, String authToken) {
        log.info("Mise à jour du pitch ID: {}", pitchId);

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        Pitch pitch = pitchRepository.findByIdAndStartupId(pitchId, startup.getId())
                .orElseThrow(() -> new PitchNotFoundException(pitchId));

        // Mettre à jour les champs
        pitch.setProbleme(request.getProbleme());
        pitch.setSolution(request.getSolution());
        pitch.setCible(request.getCible());
        pitch.setAvantage(request.getAvantage());

        // Régénérer le pitch avec Hugging Face
        String newPitch = aiService.generatePitch(
                request.getProbleme(),
                request.getSolution(),
                request.getCible(),
                request.getAvantage(),
                startup,
                pitch.getType()
        );

        pitch.setPitchGenere(newPitch);

        Pitch updatedPitch = pitchRepository.save(pitch);
        log.info("Pitch mis à jour avec succès");

        return mapToResponseDTO(updatedPitch);
    }

    @Override
    public void deletePitch(UUID pitchId, String authToken) {
        log.info("Suppression du pitch ID: {}", pitchId);

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        if (!pitchRepository.existsByIdAndStartupId(pitchId, startup.getId())) {
            throw new PitchNotFoundException(pitchId);
        }

        pitchRepository.deleteById(pitchId);
        log.info("Pitch supprimé avec succès");
    }

    @Override
    public PitchResponseDTO toggleFavorite(UUID pitchId, String authToken) {
        log.info("Toggle favori pour pitch ID: {}", pitchId);

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        Pitch pitch = pitchRepository.findByIdAndStartupId(pitchId, startup.getId())
                .orElseThrow(() -> new PitchNotFoundException(pitchId));

        pitch.setIsFavorite(!pitch.getIsFavorite());

        Pitch updatedPitch = pitchRepository.save(pitch);
        log.info("Favori modifié: {}", updatedPitch.getIsFavorite());

        return mapToResponseDTO(updatedPitch);
    }

    @Override
    public PitchResponseDTO ratePitch(UUID pitchId, Integer rating, String authToken) {
        log.info("Notation du pitch ID: {} avec note: {}", pitchId, rating);

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Le rating doit être entre 1 et 5");
        }

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        Pitch pitch = pitchRepository.findByIdAndStartupId(pitchId, startup.getId())
                .orElseThrow(() -> new PitchNotFoundException(pitchId));

        pitch.setRating(rating);

        Pitch updatedPitch = pitchRepository.save(pitch);
        log.info("Pitch noté avec succès");

        return mapToResponseDTO(updatedPitch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PitchResponseDTO> getFavoritePitchs(String authToken) {
        log.info("Récupération des pitchs favoris");

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        List<Pitch> favoritePitchs = pitchRepository
                .findByStartupIdAndIsFavoriteTrueOrderByCreatedAtDesc(startup.getId());

        log.info("Nombre de pitchs favoris: {}", favoritePitchs.size());

        return favoritePitchs.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PitchResponseDTO> getMyPitchsPaginated(String authToken, Pageable pageable) {
        log.info("Récupération des pitchs avec pagination");

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        Page<Pitch> pitchsPage = pitchRepository.findByStartupId(startup.getId(), pageable);

        return pitchsPage.map(this::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public PitchStatsDTO getMyPitchStats(String authToken) {
        log.info("Récupération des statistiques des pitchs");

        UserDTO user = authServiceClient.getCurrentUser(authToken);
        StartupDTO startup = startupServiceClient.getMyStartup(authToken);

        long totalPitchs = pitchRepository.countByStartupId(startup.getId());

        List<Pitch> allPitchs = pitchRepository.findByStartupIdOrderByCreatedAtDesc(startup.getId());
        long favoritePitchs = allPitchs.stream()
                .filter(Pitch::getIsFavorite)
                .count();

        Double averageRating = pitchRepository.getAverageRatingForStartup(startup.getId());

        Map<String, Long> pitchsByType = new HashMap<>();
        List<Object[]> typeStats = pitchRepository.countByTypeForStartup(startup.getId());
        for (Object[] stat : typeStats) {
            pitchsByType.put(stat[0].toString(), (Long) stat[1]);
        }

        return PitchStatsDTO.builder()
                .totalPitchs(totalPitchs)
                .favoritePitchs(favoritePitchs)
                .averageRating(averageRating != null ? averageRating : 0.0)
                .pitchsByType(pitchsByType)
                .build();
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