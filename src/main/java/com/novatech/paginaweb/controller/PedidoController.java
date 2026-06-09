package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.service.ExcelReportService;
import com.novatech.paginaweb.service.PdfReportService;
import com.novatech.paginaweb.service.PedidoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "http://localhost:5173")
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
            if (pedido.getUsuario() == null || pedido.getUsuario().getId() == null) {
                return ResponseEntity.badRequest().body("Error: El usuario es obligatorio para registrar el pedido.");
            }
            if (pedido.getDetalles() == null || pedido.getDetalles().isEmpty()) {
                return ResponseEntity.badRequest().body("Error: El pedido debe contener al menos un artículo.");
            }
            return ResponseEntity.ok(pedidoService.crearPedido(pedido));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * UNIFICADO: GET /api/pedidos
     * Soporta tanto listado total como filtrado por rango de fechas opcional (Vista Administrador).
     */
    @GetMapping
    public ResponseEntity<List<Pedido>> listarTodos(
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        LocalDateTime desde = (inicio != null) ? inicio.atStartOfDay() : null;
        LocalDateTime hasta = (fin != null) ? fin.atTime(LocalTime.MAX) : null;

        return ResponseEntity.ok(pedidoService.listarTodos(desde, hasta));
    }

    /**
     * UNIFICADO: GET /api/pedidos/usuario/{usuarioId}
     * Soporta tanto el historial completo del usuario como el filtrado por rango de fechas opcional (Vista Cliente).
     */
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Pedido>> listarPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        LocalDateTime desde = (inicio != null) ? inicio.atStartOfDay() : null;
        LocalDateTime hasta = (fin != null) ? fin.atTime(LocalTime.MAX) : null;

        return ResponseEntity.ok(pedidoService.listarPorUsuario(usuarioId, desde, hasta));
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
    @Autowired
    private ExcelReportService excelReportService;

    /**
     * GET /api/pedidos/exportar
     * Genera y descarga un archivo Excel con los pedidos (Soporta filtrado opcional de fechas).
     */
    @GetMapping("/exportar")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportarAExcel(
            @RequestParam(value = "inicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(value = "fin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        // 1. Reutilizamos la lógica de mapeo temporal que ya tienes estructurada
        LocalDateTime desde = (inicio != null) ? inicio.atStartOfDay() : null;
        LocalDateTime hasta = (fin != null) ? fin.atTime(LocalTime.MAX) : null;

        // 2. Buscamos los registros filtrados desde tu base de datos
        List<Pedido> listaPedidos = pedidoService.listarTodos(desde, hasta);

        // 3. Pasamos los datos recolectados al motor de POI
        ByteArrayInputStream in = excelReportService.generarReportePedidos(listaPedidos);

        // 4. Configuramos las cabeceras HTTP necesarias para desencadenar la descarga en el navegador
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_pedidos.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new org.springframework.core.io.InputStreamResource(in));
    }
    @Autowired
    private PdfReportService pdfReportService;

    @GetMapping("/exportar-pdf")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportarPdf() {
        List<Pedido> pedidos = pedidoService.listarTodos(); // o el método que uses para obtener los datos

        java.io.ByteArrayInputStream in = pdfReportService.generarPdfPedidos(pedidos);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_pedidos.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF) // Tipo de contenido correcto para PDF
                .body(new org.springframework.core.io.InputStreamResource(in));
    }
}