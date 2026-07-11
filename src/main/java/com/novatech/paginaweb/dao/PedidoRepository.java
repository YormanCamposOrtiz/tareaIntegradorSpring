package com.novatech.paginaweb.dao;

import com.novatech.paginaweb.model.Pedido;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;        
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    // Listar todos los pedidos ordenados por fecha descendente
    List<Pedido> findAllByOrderByFechaDesc();

    // Filtro para el Administrador (Todos los usuarios en un rango de fechas)
    List<Pedido> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

    // Filtro para el Cliente (Un usuario específico en un rango de fechas)
    List<Pedido> findByUsuarioIdAndFechaBetweenOrderByFechaDesc(Long usuarioId, LocalDateTime inicio, LocalDateTime fin);

    List<Pedido> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

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


    @Query(
        value = "SELECT cancelar_pedido(:pedidoId)",
        nativeQuery = true
    )
    void cancelarPedidoProcedimiento(
        @Param("pedidoId") Long pedidoId
    );
}