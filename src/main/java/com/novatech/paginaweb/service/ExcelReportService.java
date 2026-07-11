package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.model.Venta;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ExcelReportService {
    ByteArrayInputStream generarReportePedidos(List<Pedido> pedidos);
    ByteArrayInputStream generarReporteCompras(List<Compra> compras);
    ByteArrayInputStream generarReporteVentas(List<Venta> ventas);
    ByteArrayInputStream generarReporteProductos(List<Producto> productos);
}