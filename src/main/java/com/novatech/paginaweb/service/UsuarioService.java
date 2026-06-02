package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Usuario;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UsuarioService extends UserDetailsService {
    Usuario registrarNuevoUsuario(Usuario usuario);
    String generarTokenRecuperacion(String correo);
    void completarRecuperacion(String token, String nuevaContrasena);
}