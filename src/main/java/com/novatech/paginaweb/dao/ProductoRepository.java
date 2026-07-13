package com.novatech.paginaweb.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.novatech.paginaweb.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Aquí puedes agregar búsquedas personalizadas, por ejemplo, por nombre

    // Busca productos cuyo nombre contenga el texto ingresado ignorando mayúsculas y minúsculas
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    long countByVisibilidadTrue();

    @Query("SELECT p FROM Producto p WHERE p.stock <= COALESCE(p.stockMin, 0)")
    List<Producto> findProductosConStockBajo();

    @Query("""
        SELECT COUNT(p)
        FROM Producto p
        WHERE p.stock <= p.stockMin
    """)
    long contarStockBajo();

}