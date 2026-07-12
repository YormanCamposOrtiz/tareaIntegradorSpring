package com.novatech.paginaweb.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.novatech.paginaweb.model.Compra;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {

    List<Compra> findByUsuarioId(Long usuarioId);

    List<Compra> findByProveedorContainingIgnoreCase(String proveedor);

    List<Compra> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // IMPORTANTE: @Query obligatorio, si no Spring interpreta mal el nombre
    @Query("SELECT DISTINCT c FROM Compra c " +
           "LEFT JOIN FETCH c.usuario " +
           "LEFT JOIN FETCH c.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "ORDER BY c.fecha DESC")
    List<Compra> findAllWithDetalles();

    @Query("SELECT DISTINCT c FROM Compra c " +
           "LEFT JOIN FETCH c.usuario " +
           "LEFT JOIN FETCH c.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE c.fecha BETWEEN :inicio AND :fin " +
           "ORDER BY c.fecha DESC")
    List<Compra> findByFechaBetweenWithDetalles(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT c FROM Compra c " +
           "LEFT JOIN FETCH c.usuario " +
           "LEFT JOIN FETCH c.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE c.id = :id")
    Optional<Compra> findByIdWithDetalles(@Param("id") Long id);
}