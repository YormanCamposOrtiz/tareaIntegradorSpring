package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Usuario;
import com.novatech.paginaweb.repository.UsuarioRepository; // Importar el repositorio
import com.novatech.paginaweb.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository; // <--- ERROR CORREGIDO: Faltaba inyectar esto

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.registrarNuevoUsuario(usuario);
            return ResponseEntity.ok(Map.of(
                    "message", "Usuario registrado exitosamente",
                    "correo", nuevoUsuario.getCorreo()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // 1. Listar todos los usuarios
    @GetMapping("/usuarios")
    public ResponseEntity<List<Usuario>> listarTodos() {
        // Usamos el repositorio inyectado arriba
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // 2. Actualizar información y ROL
    @PutMapping("/usuarios/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody Usuario datosActualizados) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(datosActualizados.getNombre());
            usuario.setRol(datosActualizados.getRol());
            usuarioRepository.save(usuario);
            return ResponseEntity.ok(Map.of("message", "Usuario actualizado con éxito"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 3. Eliminar usuario
    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        // ERROR CORREGIDO: Antes decía "UsuarioRepository" (con mayúscula, como clase)
        // debe ser "usuarioRepository" (el objeto inyectado)
        usuarioRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado"));
    }
}