package com.novatech.paginaweb.repository;

import com.novatech.paginaweb.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    // Si necesitas buscar una categoría por su nombre exacto en el futuro
    Categoria findByNombre(String nombre);
}