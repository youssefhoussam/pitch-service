package ma.startup.platform.pitchservice.client;

import lombok.extern.slf4j.Slf4j;
import ma.startup.platform.pitchservice.dto.UserDTO;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthServiceFallback implements AuthServiceClient {

    @Override
    public UserDTO getCurrentUser(String token) {
        log.error("Fallback activé pour getCurrentUser - Service Auth indisponible");
        throw new RuntimeException("Service d'authentification temporairement indisponible");
    }
}
