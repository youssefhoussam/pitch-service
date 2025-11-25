package ma.startup.platform.pitchservice.repository;


import feign.Param;
import ma.startup.platform.pitchservice.model.Pitch;
import ma.startup.platform.pitchservice.model.PitchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PitchRepository extends JpaRepository<Pitch, UUID> {

    // Trouver tous les pitchs d'une startup
    List<Pitch> findByStartupIdOrderByCreatedAtDesc(UUID startupId);

    // Trouver les pitchs favoris d'une startup
    List<Pitch> findByStartupIdAndIsFavoriteTrueOrderByCreatedAtDesc(UUID startupId);

    // Trouver par type de pitch
    List<Pitch> findByStartupIdAndTypeOrderByCreatedAtDesc(UUID startupId, PitchType type);

    // Compter le nombre de pitchs d'une startup
    long countByStartupId(UUID startupId);

    // Vérifier si un pitch existe pour une startup
    boolean existsByIdAndStartupId(UUID id, UUID startupId);

    // Trouver un pitch spécifique d'une startup
    Optional<Pitch> findByIdAndStartupId(UUID id, UUID startupId);

    // Pagination des pitchs d'une startup
    Page<Pitch> findByStartupId(UUID startupId, Pageable pageable);

    // Statistiques par type
    @Query("SELECT p.type, COUNT(p) FROM Pitch p WHERE p.startupId = :startupId GROUP BY p.type")
    List<Object[]> countByTypeForStartup(@Param("startupId") UUID startupId);

    // Moyenne des ratings pour une startup
    @Query("SELECT AVG(p.rating) FROM Pitch p WHERE p.startupId = :startupId AND p.rating IS NOT NULL")
    Double getAverageRatingForStartup(@Param("startupId") UUID startupId);
}
