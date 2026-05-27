package com.novatech.paginaweb.dao;

import com.novatech.paginaweb.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Aquí puedes agregar búsquedas personalizadas, por ejemplo, por nombre
}