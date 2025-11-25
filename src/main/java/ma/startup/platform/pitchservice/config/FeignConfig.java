package ma.startup.platform.pitchservice.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    /**
     * Logger pour déboguer les appels Feign
     */
    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    /**
     * Intercepteur pour ajouter des headers communs (optionnel)
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Content-Type", "application/json");
            requestTemplate.header("Accept", "application/json");
        };
    }
}

