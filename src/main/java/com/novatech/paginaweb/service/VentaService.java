package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Venta;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaService {

   Venta registrarVenta(Venta venta);

    List<Venta> listarTodas();

    Venta buscarPorId(Long id);

    void eliminarVenta(Long id);

    // NUEVO:
    List<Venta> listarPorFechas(LocalDateTime inicio, LocalDateTime fin);
}