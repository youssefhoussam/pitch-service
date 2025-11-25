package ma.startup.platform.pitchservice.client;

import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.pitchservice.dto.StartupDTO;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component
@Slf4j
public class StartupServiceFallback implements StartupServiceClient {

    @Override
    public StartupDTO getStartupById(UUID id) {
        log.error("Fallback activé pour getStartupById - Service Startup indisponible");
        return StartupDTO.builder()
                .id(id)
                .nom("Startup inconnue")
                .secteur("Non défini")
                .description("Informations temporairement indisponibles")
                .build();
    }

    @Override
    public StartupDTO getMyStartup(String token) {
        log.error("Fallback activé pour getMyStartup - Service Startup indisponible");
        throw new RuntimeException("Service Startup temporairement indisponible");
    }
}

