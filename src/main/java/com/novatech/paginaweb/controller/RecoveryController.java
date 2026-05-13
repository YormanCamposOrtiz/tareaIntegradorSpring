package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class RecoveryController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/solicitar-recuperacion")
    public ResponseEntity<?> solicitar(@RequestBody Map<String, String> request) {
        try {
            String token = usuarioService.generarTokenRecuperacion(request.get("correo"));
            // En un proyecto real, aquí enviarías un correo.
            // Por ahora, devolvemos el token para que pruebes el flujo.
            return ResponseEntity.ok(Map.of(
                    "message", "Se ha generado el acceso para el cambio.",
                    "token", token
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/resetear-password")
    public ResponseEntity<?> resetear(@RequestBody Map<String, String> request) {
        try {
            usuarioService.completarRecuperacion(request.get("token"), request.get("nuevaContrasena"));
            return ResponseEntity.ok(Map.of("message", "Contraseña actualizada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}