package ma.startup.platform.pitchservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PitchRequestDTO {

    @NotBlank(message = "Le problème ne peut pas être vide")
    @Size(max = 500, message = "Le problème ne doit pas dépasser 500 caractères")
    private String probleme;

    @NotBlank(message = "La solution ne peut pas être vide")
    @Size(max = 500, message = "La solution ne doit pas dépasser 500 caractères")
    private String solution;

    @NotBlank(message = "La cible ne peut pas être vide")
    @Size(max = 300, message = "La cible ne doit pas dépasser 300 caractères")
    private String cible;

    @NotBlank(message = "L'avantage ne peut pas être vide")
    @Size(max = 300, message = "L'avantage ne doit pas dépasser 300 caractères")
    private String avantage;
}
