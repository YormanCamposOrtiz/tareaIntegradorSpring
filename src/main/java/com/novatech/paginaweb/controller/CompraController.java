package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@CrossOrigin(origins = "*")
public class CompraController {

    @Autowired
    private CompraService compraService;

    // POST /api/compras -> Registrar compra
    @PostMapping
    public ResponseEntity<?> registrarCompra(@RequestBody Compra compra) {
        try {
            Compra nuevaCompra = compraService.registrarCompra(compra);
            return new ResponseEntity<>(nuevaCompra, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // EL ÚNICO GETMAPPING RAÍZ NECESARIO
    @GetMapping
    public ResponseEntity<List<Compra>> listarTodas(
            @RequestParam(required = false) String inicio,
            @RequestParam(required = false) String fin) {

        // Si el frontend envía ambos parámetros de fecha, filtramos en la BD
        if (inicio != null && !inicio.isEmpty() && fin != null && !fin.isEmpty()) {
            try {
                java.time.LocalDateTime fechaInicio = java.time.LocalDate.parse(inicio).atStartOfDay();
                java.time.LocalDateTime fechaFin = java.time.LocalDate.parse(fin).atTime(23, 59, 59);

                return ResponseEntity.ok(compraService.listarPorFechas(fechaInicio, fechaFin));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }

        // Si no se envían fechas (carga inicial del Dashboard), devuelve todo el historial
        return ResponseEntity.ok(compraService.listarTodas());
    }

    // GET /api/compras/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Compra> buscarPorId(@PathVariable Long id) {
        return compraService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/compras/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCompra(@PathVariable Long id) {
        try {
            compraService.eliminarCompra(id);
            return ResponseEntity.ok("Compra eliminada y stock revertido correctamente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}