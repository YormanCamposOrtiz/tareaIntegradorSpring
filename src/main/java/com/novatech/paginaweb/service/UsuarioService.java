package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Usuario;
import com.novatech.paginaweb.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

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
                .password(usuario.getContrasena()) // {noop} indica texto plano
                .roles(usuario.getRol()) 
                .build();
    }

    public Usuario registrarNuevoUsuario(Usuario usuario) {
        // 1. Validaciones con Guava
        Preconditions.checkArgument(!Strings.isNullOrEmpty(usuario.getNombre()), "El nombre es obligatorio");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(usuario.getCorreo()), "El correo es obligatorio");
        Preconditions.checkArgument(usuario.getContrasena().length() >= 8, "La contraseña debe tener al menos 8 caracteres");

        // 2. Verificar si el correo ya existe
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 3. Encriptar contraseña y asignar Rol
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setRol("Usuario"); // Forzamos el rol que pediste
        usuario.setIntentosFallidos(0);

        return usuarioRepository.save(usuario);
    }
}