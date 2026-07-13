package com.novatech.paginaweb.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novatech.paginaweb.model.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    // Buscar ventas de un usuario específico
    List<Venta> findByUsuarioId(Long usuarioId);

    // NUEVO: Query Method para buscar ventas entre dos fechas
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // NUEVO: trae todo lo que el JSON necesita
    @Query("SELECT DISTINCT v FROM Venta v " +
           "LEFT JOIN FETCH v.usuario " +
           "LEFT JOIN FETCH v.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "ORDER BY v.fecha DESC")
    List<Venta> findAllWithDetalles();

    @Query("SELECT DISTINCT v FROM Venta v " +
           "LEFT JOIN FETCH v.usuario " +
           "LEFT JOIN FETCH v.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE v.fecha BETWEEN :inicio AND :fin " +
           "ORDER BY v.fecha DESC")
    List<Venta> findByFechaBetweenWithDetalles(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT v FROM Venta v " +
           "LEFT JOIN FETCH v.usuario " +
           "LEFT JOIN FETCH v.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE v.id = :id")
    java.util.Optional<Venta> findByIdWithDetalles(@Param("id") Long id);
    @Query("""
        SELECT COALESCE(SUM(v.total), 0)
        FROM Venta v
        WHERE v.fecha >= :inicio
          AND v.fecha < :fin
    """)
    Double obtenerVentasHoy(LocalDateTime inicio, LocalDateTime fin);
}