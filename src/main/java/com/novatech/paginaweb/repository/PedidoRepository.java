package com.novatech.paginaweb.repository;

import com.novatech.paginaweb.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    // Para que el cliente vea solo SUS pedidos
    List<Pedido> findByClienteIdOrderByFechaDesc(Long clienteId);
    
    // Para que el admin vea todos los que están por enviar
    List<Pedido> findByEstado(String estado);
}