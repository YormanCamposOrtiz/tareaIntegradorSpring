package com.novatech.paginaweb.config; // Verifica que este sea tu paquete real

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Permite todas las rutas
                .allowedOrigins("http://localhost:5173") // Tu puerto de React
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}