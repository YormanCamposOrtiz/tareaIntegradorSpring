package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:5173") // 👈 Úsalo exactamente igual que en el LoginController
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    /**
     * POST /api/pedidos
     * Registra un nuevo pedido ejecutando el procedimiento almacenado en PostgreSQL.
     */
    @PostMapping
    public ResponseEntity<?> crearPedido(@RequestBody Pedido pedido) {
        try {
            // Validaciones básicas de integridad antes de mandar a la BD
            if (pedido.getUsuario() == null || pedido.getUsuario().getId() == null) {
                return ResponseEntity.badRequest().body("Error: El usuario es obligatorio para registrar el pedido.");
            }
            if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El pedido debe contener al menos un artículo.");
            }

            // Invocamos al servicio que ejecuta el procedimiento y nos devuelve el Pedido persistido
            Pedido nuevoPedido = pedidoService.crearPedido(pedido);
            
            // Retornamos un estado 201 Created con el objeto completo
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPedido);

        } catch (RuntimeException e) {
            // Captura excepciones como 'Stock insuficiente para el producto ID X' lanzadas por PostgreSQL
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Hubo un error inesperado en el servidor al procesar el pedido.");
        }
    }

    /**
     * GET /api/pedidos
     * Lista todos los pedidos registrados en el sistema (Útil para la vista de Administrador).
     */
    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    /**
     * GET /api/pedidos/usuario/{usuarioId}
     * Lista el historial de pedidos de un usuario específico para renderizarlo en su perfil de React.
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Pedido>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.listarPorUsuario(usuarioId));
    }

    /**
     * GET /api/pedidos/{id}
     * Obtiene el detalle completo de un pedido individual usando su ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPorId(@PathVariable Long id) {
        return pedidoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/pedidos/{id}/estado
     * Actualiza el estado del pedido (Ej. de 'PREPARANDO' a 'ENVIADO' o 'ENTREGADO').
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestParam String nuevoEstado) {
        try {
            Pedido pedidoActualizado = pedidoService.actualizarEstado(id, nuevoEstado);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/pedidos/{id}/cancelar
     * Cancela un pedido si cumple con las reglas del negocio.
     */
    @PutMapping("/{id}/cancelar")
    public ResponseEntity<?> cancelarPedido(@PathVariable Long id) {
        try {
            pedidoService.cancelarPedido(id);
            return ResponseEntity.ok("El pedido con ID " + id + " ha sido cancelado con éxito.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}