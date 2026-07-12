package com.novatech.paginaweb.dao;

import com.novatech.paginaweb.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    long countByVisibilidadTrue();

    @Query("SELECT p FROM Producto p WHERE p.stock <= COALESCE(p.stockMin, 0)")
    List<Producto> findProductosConStockBajo();
}