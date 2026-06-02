package com.novatech.paginaweb.dao;

import com.novatech.paginaweb.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    // Buscar ventas de un usuario específico
    List<Venta> findByUsuarioId(Long usuarioId);

    // NUEVO: Query Method para buscar ventas entre dos fechas
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}