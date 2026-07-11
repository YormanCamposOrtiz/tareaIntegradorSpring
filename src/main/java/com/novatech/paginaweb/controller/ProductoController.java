package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.service.ExcelReportService;
import com.novatech.paginaweb.service.PdfReportService;
import com.novatech.paginaweb.service.ProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "http://localhost:5173") // Para que tu React pueda conectar
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired
    private ExcelReportService excelReportService;

    // ENDPOINT UNIFICADO PARA VER TODOS LOS PRODUCTOS O FILTRAR POR NOMBRE
    @GetMapping
    public List<Producto> listarProductos(@RequestParam(required = false) String nombre) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            return productoService.buscarPorNombre(nombre);
        }
        // Si no viene el parámetro 'nombre', devuelve la lista completa de visibles
        return productoService.listarVisibles();
    }

    @GetMapping("/stock-bajo")
    public ResponseEntity<Long> obtenerCantidadStockBajo() {
        return ResponseEntity.ok(productoService.contarProductosStockBajo());
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


    @GetMapping("/exportar")
    public ResponseEntity<InputStreamResource> exportarExcel() {
        List<Producto> productos = productoService.listarTodos();
        ByteArrayInputStream in = excelReportService.generarReporteProductos(productos);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=reporte_productos.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

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

    @GetMapping("/exportar-pdf")
    public ResponseEntity<org.springframework.core.io.InputStreamResource> exportarPdf() {
        List<Producto> productos = productoService.listarVisibles();

        java.io.ByteArrayInputStream in = pdfReportService.generarPdfProductos(productos);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=inventario_productos.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(new org.springframework.core.io.InputStreamResource(in));
    }
}