package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.model.Venta;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface PdfReportService {
    ByteArrayInputStream generarPdfPedidos(List<Pedido> pedidos);
    ByteArrayInputStream generarPdfCompras(List<Compra> compras);
    ByteArrayInputStream generarPdfVentas(List<Venta> ventas);
    ByteArrayInputStream generarPdfProductos(List<Producto> productos);
}