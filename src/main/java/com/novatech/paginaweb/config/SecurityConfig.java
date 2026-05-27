package com.novatech.paginaweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // IMPORTANTE
import org.springframework.security.crypto.password.PasswordEncoder;     // IMPORTANTE
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter; // Inyectamos el filtro creado 

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(request -> {
                CorsConfiguration config = new CorsConfiguration();
                config.setAllowedOrigins(List.of("http://localhost:5173"));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.setAllowedHeaders(List.of("*"));
                config.setAllowCredentials(true);
                return config;
            }))
            .csrf(csrf -> csrf.disable()) 
            // ¡ESTO ES NUEVO E IMPORTANTE!: Desactiva la creación de sesiones en el servidor
            .sessionManagement(session -> session.sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos: permitimos el acceso total a auth y todo lo que esté bajo /api/productos y /api/categorias
                .requestMatchers("/api/auth/**").permitAll() 
                .requestMatchers("/api/productos", "/api/productos/**").permitAll() 
                .requestMatchers("/api/categorias", "/api/categorias/**").permitAll() 
                .requestMatchers("/api/ventas", "/api/ventas/**").permitAll() 

                // Endpoints protegidos por Roles
                .requestMatchers("/api/productos/guardar", "/api/productos/eliminar").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                
                // Cualquier otra ruta requiere estar logueado
                .anyRequest().authenticated() 
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        // Agrega el filtro JWT antes del filtro por defecto de Spring
        http.addFilterBefore(jwtFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}