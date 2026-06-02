package com.novatech.paginaweb.service.impl;

import com.novatech.paginaweb.dao.UsuarioRepository;
import com.novatech.paginaweb.model.Usuario;
import com.novatech.paginaweb.service.UsuarioService;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JavaMailSender mailSender;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final String CARACTERES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Cache<String, String> recoveryTokens = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        if (usuario.getBloqueadoHasta() != null &&
                usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            throw new LockedException("La cuenta esta bloqueada temporalmente.");
        }

        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getContrasena())
                .roles(usuario.getRol())
                .build();
    }
    

    @Override
    public Usuario registrarNuevoUsuario(Usuario usuario) {
        // 1. Validaciones de integridad
        Preconditions.checkArgument(!Strings.isNullOrEmpty(usuario.getNombre()), "El nombre es obligatorio");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(usuario.getCorreo()), "El correo es obligatorio");
        Preconditions.checkArgument(usuario.getContrasena() != null && usuario.getContrasena().length() >= 8,
                "La contrasena debe tener al menos 8 caracteres");

        // 2. Verificar si ya existe
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya esta registrado");
        }

        // 3. LÓGICA DE ROL DINÁMICO
        if (Strings.isNullOrEmpty(usuario.getRol())) {
            usuario.setRol("Usuario");
        }

        // 4. Seguridad y persistencia
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setIntentosFallidos(0);

        return usuarioRepository.save(usuario);
    }

    @Override
    public String generarTokenRecuperacion(String correo) {
        usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("No existe una cuenta asociada a este correo."));

        String token = generarCodigoCorto(8);
        recoveryTokens.put(token, correo);

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("Código de Recuperación - NovaTech");
        mensaje.setText("Tu código de recuperación es: " + token +
                "\n\nEste código vencerá en 5 minutos. Ingrésalo en la plataforma para cambiar tu contraseña.");

        mailSender.send(mensaje);

        return token;
    }

    @Override
    public void completarRecuperacion(String token, String nuevaContrasena) {
        String correo = recoveryTokens.getIfPresent(token);

        Preconditions.checkArgument(!Strings.isNullOrEmpty(correo), "El codigo ha expirado o es invalido.");
        Preconditions.checkArgument(nuevaContrasena.length() >= 8, "La nueva contrasena debe tener al menos 8 caracteres.");

        Usuario usuario = usuarioRepository.findByCorreo(correo).get();

        usuario.setContrasena(passwordEncoder.encode(nuevaContrasena));
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);

        usuarioRepository.save(usuario);
        recoveryTokens.invalidate(token);
    }

    // --- Métodos privados utilitarios ---
    private String generarCodigoCorto(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }
        return sb.toString();
    }
}