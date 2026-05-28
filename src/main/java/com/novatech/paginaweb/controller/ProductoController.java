package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.service.ProductoService;

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
    private ProductoService productoService;

    // ENDPOINT PARA VER TODOS LOS PRODUCTOS (ALMACÉN)
    @GetMapping
    public List<Producto> listarProductos() {
        // Esto devuelve la lista completa incluyendo el objeto Categoria
        return productoService.listarVisibles();
    }

    // Opcional: Obtener un solo producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Long id) {
    Producto producto = productoService.buscarPorId(id);
    
    if (producto != null) {
        return ResponseEntity.ok(producto);
    } else {
        return ResponseEntity.notFound().build();
    }
}

    // ENDPOINT PARA GUARDAR O ACTUALIZAR PRODUCTO
    @PostMapping
    public ResponseEntity<Producto> guardar(@RequestBody Producto producto) {
        try {
            return new ResponseEntity<>(productoService.guardar(producto), HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ENDPOINT PARA ELIMINAR (Para que funcione el botón de Trash)
    // En lugar de borrar, llamamos al método que cambia la visibilidad
    @PatchMapping("/{id}/visibilidad")
    public ResponseEntity<Void> cambiarVisibilidad(@PathVariable Long id) {
        try {
            productoService.eliminar(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
