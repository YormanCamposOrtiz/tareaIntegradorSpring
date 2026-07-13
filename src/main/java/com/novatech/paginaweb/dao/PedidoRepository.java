package com.novatech.paginaweb.dao;

import com.novatech.paginaweb.model.Pedido;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByFechaDesc();

    List<Pedido> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

    List<Pedido> findByUsuarioIdAndFechaBetweenOrderByFechaDesc(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    List<Pedido> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Pedido> findByEstadoAndFechaBefore(String estado, LocalDateTime fecha);

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.usuario " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "ORDER BY p.fecha DESC")
    List<Pedido> findAllWithDetalles();

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.usuario " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE p.fecha BETWEEN :inicio AND :fin " +
           "ORDER BY p.fecha DESC")
    List<Pedido> findByFechaBetweenWithDetalles(
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.usuario " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE p.usuario.id = :usuarioId " +
           "ORDER BY p.fecha DESC")
    List<Pedido> findByUsuarioIdWithDetalles(@Param("usuarioId") Long usuarioId);

    @Query("SELECT DISTINCT p FROM Pedido p " +
           "LEFT JOIN FETCH p.usuario " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE p.usuario.id = :usuarioId AND p.fecha BETWEEN :inicio AND :fin " +
           "ORDER BY p.fecha DESC")
    List<Pedido> findByUsuarioIdAndFechaBetweenWithDetalles(
            @Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT p FROM Pedido p " +
           "LEFT JOIN FETCH p.usuario " +
           "LEFT JOIN FETCH p.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE p.id = :id")
    Optional<Pedido> findByIdWithDetalles(@Param("id") Long id);

    @Query(
        value = """
            SELECT procesar_registro_pedido(
                :p_usuario_id,
                :p_direccion_envio,
                :p_observaciones,
                :p_productos_ids,
                :p_cantidades
            )
        """,
        nativeQuery = true
    )
    Long registrarPedidoProcedimiento(
        @Param("p_usuario_id") Long usuarioId,
        @Param("p_direccion_envio") String direccionEnvio,
        @Param("p_observaciones") String observaciones,
        @Param("p_productos_ids") Long[] productosIds,
        @Param("p_cantidades") Integer[] cantidades
    );

    @Modifying
    @Query(
        value = "SELECT cancelar_pedido(:pedidoId)",
        nativeQuery = true
    )
    void cancelarPedidoProcedimiento(
        @Param("pedidoId") Long pedidoId
    );
}
