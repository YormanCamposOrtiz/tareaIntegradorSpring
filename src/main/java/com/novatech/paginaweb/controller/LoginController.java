package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Usuario;
import com.novatech.paginaweb.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class LoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private static final int MAX_INTENTOS = 3;
    private static final int TIEMPO_BLOQUEO_MINUTOS = 15;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        // CORRECCIÓN: Usar las llaves en español que vienen desde React
        String correo = loginData.get("correo"); 
        String contrasena = loginData.get("contrasena");

        // Depuración rápida: esto aparecerá en tu consola de Spring
        System.out.println("Intentando login para: " + correo);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo);

        if (usuarioOpt.isEmpty()) {
            // No revelamos si el correo existe o no por seguridad
            return ResponseEntity.status(401).body(Map.of("message", "Credenciales incorrectas"));
        }

        Usuario usuario = usuarioOpt.get();

        // 1. Verificar bloqueo
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(LocalDateTime.now())) {
            return ResponseEntity.status(403).body(Map.of("message", "Cuenta bloqueada temporalmente. Intente más tarde."));
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        // 2. Validar contraseña
        if (encoder.matches(contrasena, usuario.getContrasena())) {
            // LOGIN EXITOSO
            usuario.setIntentosFallidos(0);
            usuario.setBloqueadoHasta(null);
            usuarioRepository.save(usuario);

            Map<String, Object> response = new HashMap<>();
            response.put("correo", usuario.getCorreo()); // También en español aquí
            response.put("nombre", usuario.getNombre());
            response.put("rol", usuario.getRol());
            response.put("success", true);
            return ResponseEntity.ok(response);
        } else {
            // FALLO DE LOGIN
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
                    data.put("nombre", usuario.getNombre());
                    data.put("correo", usuario.getCorreo());
                    data.put("rol", usuario.getRol());
                    return ResponseEntity.ok(data);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}