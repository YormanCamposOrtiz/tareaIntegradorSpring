package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Categoria;
import java.util.List;

public interface CategoriaService {

    List<Categoria> listarTodas();
    // Buscar una categoría específica por su ID
    Categoria obtenerPorId(Long id);
    // Guardar o actualizar una categoría
    Categoria guardar(Categoria categoria);
    // Eliminar una categoría
    void eliminar(Long id);
}