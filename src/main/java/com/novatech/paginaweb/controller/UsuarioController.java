package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Usuario;
import com.novatech.paginaweb.dao.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/perfil")
@CrossOrigin(origins = "http://localhost:5173")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    //Acutaliza la dirección y el teléfono del usuario 

    @PutMapping("/{id}/datos")
    public ResponseEntity<?> actualizarPerfil(@PathVariable Long id, @RequestBody Map<String, String> datos) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuario no encontrado.");
        }

        Usuario usuario = usuarioOpt.get();
        
        if (datos.containsKey("direccion")) usuario.setDireccion(datos.get("direccion"));
        if (datos.containsKey("nombre")) usuario.setNombre(datos.get("nombre"));
        if (datos.containsKey("telefono")) usuario.setTelefono(datos.get("telefono"));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(usuarioGuardado);
    }

    //Cambia la contraseña aplicando la encriptación BCrypt
    
    @PutMapping("/{id}/contrasena")
    public ResponseEntity<?> cambiarContrasena(@PathVariable Long id, @RequestBody Map<String, String> datos) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Usuario no encontrado.");
        }

        String nuevaClave = datos.get("nuevaContrasena");
        if (nuevaClave == null || nuevaClave.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Error: La contraseña no puede estar vacía.");
        }

        Usuario usuario = usuarioOpt.get();
        String claveEncriptada = passwordEncoder.encode(nuevaClave);
        usuario.setContrasena(claveEncriptada); 
        
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("Contraseña actualizada con éxito.");
    }
}