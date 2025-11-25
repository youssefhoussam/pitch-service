package ma.startup.platform.pitchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ma.startup.platform.pitchservice.model.PitchType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PitchResponseDTO {
    private UUID id;
    private UUID startupId;
    private String probleme;
    private String solution;
    private String cible;
    private String avantage;
    private String pitchGenere;
    private PitchType type;
    private Integer rating;
    private Boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
