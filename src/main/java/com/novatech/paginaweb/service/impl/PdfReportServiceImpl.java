package com.novatech.paginaweb.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.model.Venta;
import com.novatech.paginaweb.service.PdfReportService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportServiceImpl implements PdfReportService {

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private void agregarCeldaCabecera(PdfPTable tabla, String texto) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setBackgroundColor(new java.awt.Color(41, 128, 185)); // Azul Corporativo
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        tabla.addCell(cell);
    }

    private void agregarCeldaNormal(PdfPTable tabla, String texto, int alineacion) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 9, java.awt.Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        tabla.addCell(cell);
    }

    private void agregarEncabezadoDocumento(Document document, String tituloReporte) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, java.awt.Color.DARK_GRAY);
        Paragraph title = new Paragraph(tituloReporte + " - NOVATECH", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.GRAY);
        Paragraph fechaActual = new Paragraph("Emitido el: " + LocalDateTime.now().format(dateFormatter), subFont);
        fechaActual.setAlignment(Element.ALIGN_RIGHT);
        fechaActual.setSpacingAfter(20);
        document.add(fechaActual);
    }

    @Override
    public ByteArrayInputStream generarPdfPedidos(List<Pedido> pedidos) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoDocumento(document, "REPORTE DE PEDIDOS");

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 2.3f, 2.5f, 3.0f, 1.8f, 1.5f});

            agregarCeldaCabecera(table, "ID Pedido");
            agregarCeldaCabecera(table, "Fecha");
            agregarCeldaCabecera(table, "Cliente");
            agregarCeldaCabecera(table, "Dirección Envío");
            agregarCeldaCabecera(table, "Estado");
            agregarCeldaCabecera(table, "Total");

            for (Pedido p : pedidos) {
                agregarCeldaNormal(table, String.valueOf(p.getId()), Element.ALIGN_CENTER);
                agregarCeldaNormal(table, p.getFecha() != null ? p.getFecha().format(dateFormatter) : "---", Element.ALIGN_CENTER);
                agregarCeldaNormal(table, p.getUsuario() != null ? p.getUsuario().getNombre() : "N/A", Element.ALIGN_LEFT);
                agregarCeldaNormal(table, p.getDireccionEnvio(), Element.ALIGN_LEFT);
                agregarCeldaNormal(table, p.getEstado(), Element.ALIGN_CENTER);
                agregarCeldaNormal(table, String.format("S/. %.2f", p.getTotal()), Element.ALIGN_RIGHT);
            }
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar PDF de Pedidos: " + e.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream generarPdfProductos(List<Producto> productos) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoDocumento(document, "INVENTARIO DE PRODUCTOS");

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.0f, 3.5f, 1.5f, 1.5f, 1.5f});

            agregarCeldaCabecera(table, "ID");
            agregarCeldaCabecera(table, "Nombre del Producto");
            agregarCeldaCabecera(table, "Stock");
            agregarCeldaCabecera(table, "P. Compra");
            agregarCeldaCabecera(table, "P. Venta");

            for (Producto p : productos) {
                agregarCeldaNormal(table, String.valueOf(p.getId()), Element.ALIGN_CENTER);
                agregarCeldaNormal(table, p.getNombre(), Element.ALIGN_LEFT);
                agregarCeldaNormal(table, String.valueOf(p.getStock()), Element.ALIGN_CENTER);
                agregarCeldaNormal(table, String.format("S/. %.2f", p.getPrecio_compra()), Element.ALIGN_RIGHT);
                agregarCeldaNormal(table, String.format("S/. %.2f", p.getPrecio_venta()), Element.ALIGN_RIGHT);
            }
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar PDF de Productos: " + e.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream generarPdfVentas(List<Venta> ventas) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoDocumento(document, "REPORTE DE VENTAS");

            // Tabla reajustada a 3 columnas fijas (Sin comprobantes)
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.0f, 5.0f, 3.0f});

            agregarCeldaCabecera(table, "ID Venta");
            agregarCeldaCabecera(table, "Fecha");
            agregarCeldaCabecera(table, "Total");

            for (Venta v : ventas) {
                agregarCeldaNormal(table, String.valueOf(v.getId()), Element.ALIGN_CENTER);
                agregarCeldaNormal(table, v.getFecha() != null ? v.getFecha().format(dateFormatter) : "---", Element.ALIGN_CENTER);
                agregarCeldaNormal(table, String.format("S/. %.2f", v.getTotal() != null ? v.getTotal() : 0.0), Element.ALIGN_RIGHT);
            }

            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar PDF de Ventas: " + e.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    @Override
    public ByteArrayInputStream generarPdfCompras(List<Compra> compras) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();
            agregarEncabezadoDocumento(document, "REPORTE DE COMPRAS");

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.5f, 2.5f, 4.0f, 2.0f});

            agregarCeldaCabecera(table, "ID Compra");
            agregarCeldaCabecera(table, "Fecha");
            agregarCeldaCabecera(table, "Proveedor");
            agregarCeldaCabecera(table, "Total");

            for (Compra c : compras) {
                agregarCeldaNormal(table, String.valueOf(c.getId()), Element.ALIGN_CENTER);
                agregarCeldaNormal(table, c.getFecha() != null ? c.getFecha().format(dateFormatter) : "---", Element.ALIGN_CENTER);
                agregarCeldaNormal(table, c.getProveedor() != null ? c.getProveedor() : "N/A", Element.ALIGN_LEFT);
                agregarCeldaNormal(table, String.format("S/. %.2f", c.getTotal() != null ? c.getTotal() : 0.0), Element.ALIGN_RIGHT);
            }
            document.add(table);
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error al generar PDF de Compras: " + e.getMessage());
        }
        return new ByteArrayInputStream(out.toByteArray());
    }
}