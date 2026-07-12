package com.novatech.paginaweb.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.novatech.paginaweb.config.JwtUtil;
import com.novatech.paginaweb.dao.UsuarioRepository;
import com.novatech.paginaweb.model.Usuario;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private JwtUtil jwtUtil;

    private static final int MAX_INTENTOS = 5;
    private static final int TIEMPO_BLOQUEO_MINUTOS = 5;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        String correo = loginData.get("correo");
        String contrasena = loginData.get("contrasena");

        System.out.println("🔐 Intentando login para: " + correo);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("message", "Credenciales incorrectas"));
        }

        Usuario usuario = usuarioOpt.get();

        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(403).body(Map.of("message", "Cuenta bloqueada temporalmente."));
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (encoder.matches(contrasena, usuario.getContrasena())) {
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuarioRepository.save(usuario);

            // ==================== GENERACIÓN DEL TOKEN JWT ====================
            UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(usuario.getCorreo())
                .password(usuario.getContrasena())
                .roles(usuario.getRol())                    // ← Sin replace("ROLE_")
                .build();

            String token = jwtUtil.generateToken(userDetails);

            System.out.println("✅ TOKEN GENERADO EXITOSAMENTE para: " + usuario.getCorreo());
            
            Map<String, Object> response = new HashMap<>();
            response.put("correo", usuario.getCorreo());
            response.put("nombre", usuario.getNombre());
            response.put("rol", usuario.getRol());
            response.put("success", true);
            response.put("id", usuario.getId());
            response.put("token", token);

            System.out.println("✅ Login exitoso - Token generado");
            return ResponseEntity.ok(response);
        } else {
            procesarIntentoFallido(usuario);
            return ResponseEntity.status(401).body(Map.of("message", "Credenciales incorrectas"));
        }
    }

    private void procesarIntentoFallido(Usuario usuario) {
        int intentos = (usuario.getIntentosFallidos() == null ? 0 : usuario.getIntentosFallidos()) + 1;
        usuario.setIntentosFallidos(intentos);

        if (intentos >= MAX_INTENTOS) {
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(TIEMPO_BLOQUEO_MINUTOS));
        }
        usuarioRepository.save(usuario);
    }

    @GetMapping("/buscar")
    public ResponseEntity<?> buscarUsuario(@RequestParam String correo) {
        return usuarioRepository.findByCorreo(correo)
                .map(usuario -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("id", usuario.getId());
                    data.put("nombre", usuario.getNombre());
                    data.put("correo", usuario.getCorreo());
                    data.put("rol", usuario.getRol());
                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}