package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Categoria;
import com.novatech.paginaweb.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Obtener todas las categorías para mostrarlas en la web
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    // Buscar una categoría específica por su ID
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    // Guardar o actualizar una categoría
    public Categoria guardar(Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // Eliminar una categoría
    public void eliminar(Long id) {
        categoriaRepository.deleteById(id);
    }
}