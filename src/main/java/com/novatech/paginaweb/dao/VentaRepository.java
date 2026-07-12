package com.novatech.paginaweb.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.novatech.paginaweb.model.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    // Buscar ventas de un usuario específico
    List<Venta> findByUsuarioId(Long usuarioId);

    // NUEVO: Query Method para buscar ventas entre dos fechas
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}