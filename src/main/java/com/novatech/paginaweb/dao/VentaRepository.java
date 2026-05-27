package com.novatech.paginaweb.dao;

import com.novatech.paginaweb.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    // Buscar ventas de un usuario específico
    List<Venta> findByUsuarioId(Long usuarioId);
}