package com.optiplant.inventario.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de SpringDoc OpenAPI para documentación de la API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI optiplantOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OptiPlant Inventario API")
                        .description("Sistema de Inventario Multi-Sucursal - OptiPlant")
                        .version("v1"));
    }

}
