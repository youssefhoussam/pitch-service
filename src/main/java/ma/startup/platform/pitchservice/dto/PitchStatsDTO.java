package ma.startup.platform.pitchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PitchStatsDTO {
    private Long totalPitchs;
    private Long favoritePitchs;
    private Double averageRating;
    private Map<String, Long> pitchsByType;
}
