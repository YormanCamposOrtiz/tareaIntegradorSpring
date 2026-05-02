package com.novatech.paginaweb.repository;

import com.novatech.paginaweb.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    
    // Buscar todas las compras realizadas por un trabajador/usuario específico
    List<Compra> findByUsuarioId(Long usuarioId);

    // Buscar compras por nombre de proveedor (útil para reportes rápidos)
    List<Compra> findByProveedorContainingIgnoreCase(String proveedor);
}