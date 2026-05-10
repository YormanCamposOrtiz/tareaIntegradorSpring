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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDateTime;
import java.security.SecureRandom; // Importante para la aleatoriedad
import java.util.concurrent.TimeUnit;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JavaMailSender mailSender;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Generador seguro de números y letras
    private static final String CARACTERES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Cache<String, String> recoveryTokens = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)
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

    public Usuario registrarNuevoUsuario(Usuario usuario) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(usuario.getNombre()), "El nombre es obligatorio");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(usuario.getCorreo()), "El correo es obligatorio");
        Preconditions.checkArgument(usuario.getContrasena().length() >= 8, "La contrasena debe tener al menos 8 caracteres");

        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya esta registrado");
        }

        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        usuario.setRol("Usuario");
        usuario.setIntentosFallidos(0);

        return usuarioRepository.save(usuario);
    }

    // --- LÓGICA DE TOKEN CORTO ---

    private String generarCodigoCorto(int longitud) {
        StringBuilder sb = new StringBuilder(longitud);
        for (int i = 0; i < longitud; i++) {
            sb.append(CARACTERES.charAt(RANDOM.nextInt(CARACTERES.length())));
        }
        return sb.toString();
    }

    public String generarTokenRecuperacion(String correo) {
        usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("No existe una cuenta asociada a este correo."));

        // CAMBIO: Ahora genera un código de 8 caracteres
        String token = generarCodigoCorto(8);
        recoveryTokens.put(token, correo);

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correo);
        mensaje.setSubject("Recuperar Contrasena - MediExpress");
        mensaje.setText("Tu codigo de recuperacion es: " + token +
                "\nO entra aqui: http://localhost:5173/restablecer?token=" + token);

        mailSender.send(mensaje);

        return token;
    }

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
}