package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Venta;

import java.util.List;

public interface VentaService {

   Venta registrarVenta(Venta venta);

    List<Venta> listarTodas();

    Venta buscarPorId(Long id);

}