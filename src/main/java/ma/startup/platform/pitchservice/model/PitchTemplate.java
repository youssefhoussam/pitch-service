package ma.startup.platform.pitchservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "pitch_templates", schema = "pitch_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PitchTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompt;

    @Column(length = 100)
    private String secteur;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @PrePersist
    public void prePersist() {
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
}