package com.novatech.paginaweb.service.impl;

import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.model.Venta;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.service.ExcelReportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelReportServiceImpl implements ExcelReportService {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // --- MÉTODOS DE AYUDA PARA CREAR ESTILOS ---
    private CellStyle crearEstiloCabecera(Workbook workbook) {
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 11);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.CORNFLOWER_BLUE.getIndex()); // Unificado al azul de Pedidos
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THICK);
        return style;
    }

    private CellStyle crearEstiloCeldaNormal(Workbook workbook) {
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);

        CellStyle style = workbook.createCellStyle();
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    private CellStyle crearEstiloMoneda(Workbook workbook, CellStyle baseStyle) {
        CellStyle style = workbook.createCellStyle();
        style.cloneStyleFrom(baseStyle);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("\"S/.\" #,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    // --- REPORTE DE COMPRAS ---
    @Override
    public ByteArrayInputStream generarReporteCompras(List<Compra> compras) {
        String[] columnas = {"ID Compra", "Fecha", "Proveedor", "Monto Total"};
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Compras");
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dataStyle = crearEstiloCeldaNormal(workbook);
            CellStyle monedaStyle = crearEstiloMoneda(workbook, dataStyle);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Compra compra : compras) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(compra.getId());
                row.getCell(0).setCellStyle(dataStyle);

                String fecha = compra.getFecha() != null ? compra.getFecha().format(dateFormatter) : "";
                row.createCell(1).setCellValue(fecha);
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue(compra.getProveedor() != null ? compra.getProveedor() : "N/A");
                row.getCell(2).setCellStyle(dataStyle);

                Cell cellTotal = row.createCell(3);
                cellTotal.setCellValue(compra.getTotal() != null ? compra.getTotal() : 0.0);
                cellTotal.setCellStyle(monedaStyle);
            }

            for (int i = 0; i < columnas.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error en Excel Compras: " + e.getMessage());
        }
    }

    // --- REPORTE DE VENTAS (Sincronizado con tu modelo Venta.java) ---
    @Override
    public ByteArrayInputStream generarReporteVentas(List<Venta> ventas) {
        // Se removieron las columnas de Comprobantes que no existen en tu Entidad
        String[] columnas = {"ID Venta", "Fecha", "Usuario/Vendedor", "Monto Total"};
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Ventas");
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dataStyle = crearEstiloCeldaNormal(workbook);
            CellStyle monedaStyle = crearEstiloMoneda(workbook, dataStyle);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Venta venta : ventas) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(venta.getId());
                row.getCell(0).setCellStyle(dataStyle);

                String fecha = venta.getFecha() != null ? venta.getFecha().format(dateFormatter) : "";
                row.createCell(1).setCellValue(fecha);
                row.getCell(1).setCellStyle(dataStyle);

                // Jalamos el nombre del usuario asignado a la venta
                String vendedor = (venta.getUsuario() != null) ? venta.getUsuario().getNombre() : "N/A";
                row.createCell(2).setCellValue(vendedor);
                row.getCell(2).setCellStyle(dataStyle);

                Cell cellTotal = row.createCell(3);
                cellTotal.setCellValue(venta.getTotal() != null ? venta.getTotal() : 0.0);
                cellTotal.setCellStyle(monedaStyle);
            }

            for (int i = 0; i < columnas.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error en Excel Ventas: " + e.getMessage());
        }
    }

    // --- REPORTE DE PRODUCTOS (Sincronizado con tu modelo Producto.java) ---
    @Override
    public ByteArrayInputStream generarReporteProductos(List<Producto> productos) {
        // Ajustado a tus atributos reales: id, nombre, stock, precio_compra, precio_venta, visibilidad
        String[] columnas = {"ID", "Nombre del Producto", "Stock", "Precio Compra", "Precio Venta", "Estado"};
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Productos");
            CellStyle headerStyle = crearEstiloCabecera(workbook);
            CellStyle dataStyle = crearEstiloCeldaNormal(workbook);
            CellStyle monedaStyle = crearEstiloMoneda(workbook, dataStyle);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Producto prod : productos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(prod.getId());
                row.getCell(0).setCellStyle(dataStyle);

                row.createCell(1).setCellValue(prod.getNombre() != null ? prod.getNombre() : "");
                row.getCell(1).setCellStyle(dataStyle);

                row.createCell(2).setCellValue(prod.getStock() != null ? prod.getStock() : 0);
                row.getCell(2).setCellStyle(dataStyle);

                // Corregido: Llamadas exactas a tus Getters de Producto.java
                Cell cellC = row.createCell(3);
                cellC.setCellValue(prod.getPrecio_compra() != null ? prod.getPrecio_compra() : 0.0);
                cellC.setCellStyle(monedaStyle);

                Cell cellV = row.createCell(4);
                cellV.setCellValue(prod.getPrecio_venta() != null ? prod.getPrecio_venta() : 0.0);
                cellV.setCellStyle(monedaStyle);

                // Corregido: Uso correcto de isVisibilidad()
                String visible = prod.isVisibilidad() ? "VISIBLE" : "OCULTO";
                row.createCell(5).setCellValue(visible);
                row.getCell(5).setCellStyle(dataStyle);
            }

            for (int i = 0; i < columnas.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Error en Excel Productos: " + e.getMessage());
        }
    }

    // --- REPORTE DE PEDIDOS ---
    @Override
    public ByteArrayInputStream generarReportePedidos(List<Pedido> pedidos) {
        String[] columnas = {"ID Pedido", "Fecha", "Cliente", "Dirección de Envío", "Estado", "Total"};

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Pedidos");

            CellStyle headerCellStyle = crearEstiloCabecera(workbook);
            CellStyle dataCellStyle = crearEstiloCeldaNormal(workbook);
            CellStyle monedaCellStyle = crearEstiloMoneda(workbook, dataCellStyle);

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columnas.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(columnas[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Pedido pedido : pedidos) {
                Row row = sheet.createRow(rowIdx++);

                Cell cellId = row.createCell(0);
                cellId.setCellValue(pedido.getId());
                cellId.setCellStyle(dataCellStyle);

                String fechaStr = (pedido.getFecha() != null) ? pedido.getFecha().format(dateFormatter) : "";
                Cell cellFecha = row.createCell(1);
                cellFecha.setCellValue(fechaStr);
                cellFecha.setCellStyle(dataCellStyle);

                Cell cellCliente = row.createCell(2);
                String nombreCliente = (pedido.getUsuario() != null) ? pedido.getUsuario().getNombre() : "N/A";
                cellCliente.setCellValue(nombreCliente);
                cellCliente.setCellStyle(dataCellStyle);

                Cell cellDireccion = row.createCell(3);
                cellDireccion.setCellValue(pedido.getDireccionEnvio());
                cellDireccion.setCellStyle(dataCellStyle);

                Cell cellEstado = row.createCell(4);
                cellEstado.setCellValue(pedido.getEstado());
                cellEstado.setCellStyle(dataCellStyle);

                Cell cellTotal = row.createCell(5);
                cellTotal.setCellValue(pedido.getTotal() != null ? pedido.getTotal() : 0.0);
                cellTotal.setCellStyle(monedaCellStyle);
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Error al exportar los datos del reporte a Excel: " + e.getMessage());
        }
    }
}