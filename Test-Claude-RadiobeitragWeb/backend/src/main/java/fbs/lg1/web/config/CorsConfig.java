package fbs.lg1.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Gibt die REST-API für das Angular-Frontend unter {@code http://localhost:4200} frei.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Erlaubt Anfragen vom Angular-Entwicklungsserver auf alle Pfade unter {@code /api}.
     *
     * @param registry die CORS-Registrierung von Spring
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200")
                .allowedMethods("GET", "POST", "DELETE", "OPTIONS");
    }
}
