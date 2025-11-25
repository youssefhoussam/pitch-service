package ma.startup.platform.pitchservice.client;

import ma.startup.platform.pitchservice.dto.StartupDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.UUID;

@FeignClient(
        name = "startup-service",
        url = "${startup.service.url}",
        fallback = StartupServiceFallback.class
)
public interface StartupServiceClient {

    // Endpoint public - pas besoin de token
    @GetMapping("/api/startups/{id}")
    StartupDTO getStartupById(@PathVariable("id") UUID id);

    // Endpoint protégé - nécessite le token
    @GetMapping("/api/startups/me")
    StartupDTO getMyStartup(@RequestHeader("Authorization") String token);
}
