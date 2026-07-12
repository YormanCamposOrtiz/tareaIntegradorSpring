package com.novatech.paginaweb.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // IMPORTANTE
import org.springframework.security.config.http.SessionCreationPolicy;     // IMPORTANTE
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter; 

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
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/api/productos/**").permitAll()
        .requestMatchers("/api/categorias/**").permitAll()
        .requestMatchers("/api/ventas/**").permitAll()
        .requestMatchers("/api/compras/**").permitAll()
        .requestMatchers("/api/pedidos/**").permitAll()
        .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
        .requestMatchers("/api/productos/guardar", "/api/productos/eliminar").hasRole("ADMINISTRADOR")
        .anyRequest().authenticated()
        );
// Reactivar el filtro JWT si lo necesitas:
//.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}
}