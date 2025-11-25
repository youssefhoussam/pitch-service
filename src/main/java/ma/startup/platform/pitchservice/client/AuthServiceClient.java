package ma.startup.platform.pitchservice.client;

import ma.startup.platform.pitchservice.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "auth-service",
        url = "${auth.service.url}",
        fallback = AuthServiceFallback.class
)
public interface AuthServiceClient {

    @GetMapping("/api/users/me")
    UserDTO getCurrentUser(@RequestHeader("Authorization") String token);
}
