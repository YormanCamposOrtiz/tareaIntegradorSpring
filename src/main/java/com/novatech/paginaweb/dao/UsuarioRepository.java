package com.novatech.paginaweb.dao;

import com.novatech.paginaweb.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // ¡Importante!

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Spring generará la consulta automáticamente basándose en el nombre del método
    Optional<Usuario> findByCorreo(String correo);
    long countByRol(String rol);
}