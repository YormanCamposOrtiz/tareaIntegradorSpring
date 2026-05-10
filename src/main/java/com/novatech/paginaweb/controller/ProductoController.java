package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:5173") // Para que tu React pueda conectar
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    // ENDPOINT PARA VER TODOS LOS PRODUCTOS (ALMACÉN)
    @GetMapping
    public List<Producto> listarProductos() {
        // Esto devuelve la lista completa incluyendo el objeto Categoria
        return productoRepository.findAll();
    }

    // Opcional: Obtener un solo producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Long id) {
        return productoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ENDPOINT PARA GUARDAR O ACTUALIZAR PRODUCTO
    @PostMapping
    public ResponseEntity<Producto> guardarProducto(@RequestBody Producto producto) {
        try {
            // Spring Boot guardará el producto y manejará la relación con Categoria
            Producto nuevoProducto = productoRepository.save(producto);
            return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<Producto>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ENDPOINT PARA ELIMINAR (Para que funcione el botón de Trash)
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> eliminarProducto(@PathVariable Long id) {
        try {
            productoRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
