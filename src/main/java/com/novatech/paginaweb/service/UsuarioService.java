package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Usuario;
import com.novatech.paginaweb.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        // 1. Búsqueda robusta usando Optional
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        // 2. Validación de seguridad contra ataques de diccionario
        if (usuario.getBloqueadoHasta() != null && 
            usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new LockedException("La cuenta está temporalmente bloqueada por seguridad.");
        }

        // 3. Creación del objeto de seguridad para Spring Security
        return User.builder()
                .username(usuario.getCorreo())
                .password("{noop}" + usuario.getContrasena()) // {noop} indica texto plano
                .roles(usuario.getRol()) 
                .build();
    }
}