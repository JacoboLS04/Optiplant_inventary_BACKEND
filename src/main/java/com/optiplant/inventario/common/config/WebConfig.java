package com.optiplant.inventario.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración de CORS para permitir comunicación con el frontend.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = resolveAllowedOrigins();
        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    private String[] resolveAllowedOrigins() {
        String configured = System.getenv("CORS_ALLOWED_ORIGINS");
        if (configured != null && !configured.isBlank()) {
            return configured.split(",");
        }
        String property = System.getProperty("CORS_ALLOWED_ORIGINS");
        if (property != null && !property.isBlank()) {
            return property.split(",");
        }
        return new String[]{"http://localhost:5173", "http://localhost:4200"};
    }

}
