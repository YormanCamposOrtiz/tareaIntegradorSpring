package com.novatech.paginaweb.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // IMPORTANTE
import org.springframework.security.crypto.password.PasswordEncoder;     // IMPORTANTE
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import java.util.List;

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
            // CENTRALIZADO: Única configuración de CORS que necesitas
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
                
                // 1º EXCEPCIONES ESPECÍFICAS (Primero protegemos lo crítico)
                .requestMatchers("/api/productos/guardar", "/api/productos/eliminar").hasRole("ADMINISTRADOR")
                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                
                // 2º RUTAS PÚBLICAS GENERALES (Para ver productos, categorías, ventas o autenticarse)
                .requestMatchers("/api/auth/**").permitAll() 
                .requestMatchers("/api/productos", "/api/productos/**").permitAll() 
                .requestMatchers("/api/categorias", "/api/categorias/**").permitAll() 
                .requestMatchers("/api/ventas", "/api/ventas/**").permitAll() 
                .requestMatchers("/api/compras", "/api/compras/**").permitAll()
                .requestMatchers("/api/pedidos/**").permitAll()

                // 3º CUALQUIER OTRA PETICIÓN
                .anyRequest().authenticated() 
            )
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable());

        // Filtro JWT antes del filtro de autenticación nativo
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}