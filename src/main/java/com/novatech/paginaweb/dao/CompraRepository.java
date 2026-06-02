package com.novatech.paginaweb.dao;

import com.novatech.paginaweb.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    
    // Buscar todas las compras realizadas por un trabajador/usuario específico
    List<Compra> findByUsuarioId(Long usuarioId);

    // Buscar compras por nombre de proveedor (útil para reportes rápidos)
    List<Compra> findByProveedorContainingIgnoreCase(String proveedor);

    // NUEVO: Filtrar compras entre dos fechas/horas
    List<Compra> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
}