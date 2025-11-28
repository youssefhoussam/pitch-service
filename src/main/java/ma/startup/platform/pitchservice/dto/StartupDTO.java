package ma.startup.platform.pitchservice.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;       // ✅ java.time
import java.time.LocalDateTime;   // ✅ java.time
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StartupDTO {
    private UUID id;
    private UUID userId;
    private String nom;
    private String secteur;
    private String description;
    private String tags;
    private Integer profileCompletion;
    private String logo;
    private String siteWeb;
    private LocalDate dateCreation;      // ✅ java.time.LocalDate
    private LocalDateTime createdAt;     // ✅ java.time.LocalDateTime
    private Integer teamCount;
    private Integer milestonesCount;
}