package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.dao.CategoriaRepository;
import com.novatech.paginaweb.model.Categoria;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@CrossOrigin(origins = "http://localhost:5173")
public class CategoriaController {

    @Autowired
    private CategoriaRepository categoriaRepository;

    // Listar todas las categorías
    @GetMapping
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAll();
    }

    // CREAR una nueva categoría
    @PostMapping
    public Categoria crearCategoria(@RequestBody Categoria categoria) {
        return categoriaRepository.save(categoria);
    }

    // ELIMINAR una categoría por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Long id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return ResponseEntity.noContent().build(); // Retorna 204 si se eliminó
        } else {
            return ResponseEntity.notFound().build(); // Retorna 404 si no existe
        }
    }
}