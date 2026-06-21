package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Venta;
import com.novatech.paginaweb.service.ExcelReportService;
import com.novatech.paginaweb.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*") // Permite la comunicación fluida con tu frontend en React
public class VentaController {

    @Autowired
    private VentaService ventaService;

    /**
     * POST /api/ventas
     * Registra una nueva venta procesando el stock de forma segura.
     * Enlaza directamente con el método handleSaveVenta del botón "Guardar y Registrar Venta" de tu React.
     */
    @PostMapping
    public ResponseEntity<?> registrarVenta(@RequestBody Venta venta) {
        try {
            // Llama a tu lógica transaccional que valida, descuenta stock y guarda
            Venta ventaRegistrada = ventaService.registrarVenta(venta);
            return new ResponseEntity<>(ventaRegistrada, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            // Si el stock es insuficiente o el producto no existe, captura la excepción y envía el mensaje al alert() de React
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Ocurrió un error inesperado en el servidor al procesar la venta."));
        }
    }
    @Autowired
    private ExcelReportService excelReportService;

    @GetMapping("/exportar")
    public ResponseEntity<InputStreamResource> exportarExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        List<Venta> ventas;
        if (inicio != null && fin != null) {
            ventas = ventaService.listarPorFechas(inicio, fin);
        } else {
            ventas = ventaService.listarTodas();
        }

        ByteArrayInputStream in = excelReportService.generarReporteVentas(ventas);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_ventas.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
    /**
     * GET /api/ventas
     * UNIFICADO: Maneja tanto el historial completo como el filtro por fechas en un solo endpoint
     * Evita el error de Ambiguous mapping.
     */
    @GetMapping
    public ResponseEntity<List<Venta>> listarTodas(
            @RequestParam(required = false) String inicio,
            @RequestParam(required = false) String fin) {

        // Si el frontend envía parámetros de fecha, filtramos por rango
        if (inicio != null && !inicio.isEmpty() && fin != null && !fin.isEmpty()) {
            try {
                java.time.LocalDateTime fechaInicio = java.time.LocalDate.parse(inicio).atStartOfDay();
                java.time.LocalDateTime fechaFin = java.time.LocalDate.parse(fin).atTime(23, 59, 59);

                return ResponseEntity.ok(ventaService.listarPorFechas(fechaInicio, fechaFin));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
            }
        }

        // Si no se envían fechas (carga inicial), funciona de manera normal devolviendo todo el historial
        return ResponseEntity.ok(ventaService.listarTodas());
    }

    /**
     * GET /api/ventas/{id}
     * Busca una venta por su ID junto con todos sus detalles asociados.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Long id) {
        Venta venta = ventaService.buscarPorId(id);
        if (venta == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse("Venta no encontrada"));
        }
        return ResponseEntity.ok(venta);
    }

    /**
     * DELETE /api/ventas/{id}
     * Elimina una venta restaurando el stock de los productos.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarVenta(@PathVariable Long id) {
        try {
            ventaService.eliminarVenta(id);
            return ResponseEntity.ok(new ErrorResponse("¡Venta eliminada con éxito y stock restaurado!"));
        } catch (RuntimeException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error inesperado al intentar eliminar la venta."));
        }
    }

    // Clase interna útil para estructurar los mensajes de error en formato JSON para el frontend
    public static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
    @Autowired
    private PdfReportService pdfReportService;

    @GetMapping("/exportar-pdf")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportarPdf() {
        List<Venta> ventas = ventaService.listarTodas();

        java.io.ByteArrayInputStream in = pdfReportService.generarPdfVentas(ventas);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=inventario_productos.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(new org.springframework.core.io.InputStreamResource(in));
    }
}