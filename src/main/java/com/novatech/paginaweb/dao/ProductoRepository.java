package com.novatech.paginaweb.dao;

import com.novatech.paginaweb.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Aquí puedes agregar búsquedas personalizadas, por ejemplo, por nombre

    // Busca productos cuyo nombre contenga el texto ingresado ignorando mayúsculas y minúsculas
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

}