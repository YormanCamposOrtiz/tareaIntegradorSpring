package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Usuario;
import com.novatech.paginaweb.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")

public class RegistroController {
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarNuevoUsuario(usuario);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario registrado exitosamente",
                    "correo", nuevoUsuario.getCorreo()
            ));
        } catch (IllegalArgumentException e) {
            // Error de validación de Guava
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}
