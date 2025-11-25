package ma.startup.platform.pitchservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pitchs", schema = "pitch_schema")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pitch {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "startup_id", nullable = false)
    private UUID startupId;

    @Column(nullable = false, length = 500)
    private String probleme;

    @Column(nullable = false, length = 500)
    private String solution;

    @Column(nullable = false, length = 300)
    private String cible;

    @Column(nullable = false, length = 300)
    private String avantage;

    @Column(name = "pitch_genere", nullable = false, columnDefinition = "TEXT")
    private String pitchGenere;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PitchType type;

    @Column
    private Integer rating;

    @Column(name = "is_favorite")
    private Boolean isFavorite = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.isFavorite == null) {
            this.isFavorite = false;
        }
        if (this.type == null) {
            this.type = PitchType.ELEVATOR;
        }
    }
}
