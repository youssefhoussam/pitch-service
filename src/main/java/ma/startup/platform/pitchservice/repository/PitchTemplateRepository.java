package ma.startup.platform.pitchservice.repository;

import ma.startup.platform.pitchservice.model.PitchTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PitchTemplateRepository extends JpaRepository<PitchTemplate, UUID> {

    // Trouver les templates actifs
    List<PitchTemplate> findByIsActiveTrue();

    // Trouver par secteur
    List<PitchTemplate> findBySecteurAndIsActiveTrue(String secteur);

    // Trouver template par nom
    Optional<PitchTemplate> findByNomAndIsActiveTrue(String nom);
}
