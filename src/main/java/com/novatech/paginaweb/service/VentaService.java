package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Venta;
import com.novatech.paginaweb.model.DetalleVenta;
import com.novatech.paginaweb.repository.VentaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Transactional
    public Venta registrarVenta(Venta venta) {
        double totalVenta = 0;

        // Recorremos los detalles para calcular el total y establecer la relación
        for (DetalleVenta detalle : venta.getDetalles()) {
            // Calculamos subtotal por si no viene del frontend
            double subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
            detalle.setSubtotal(subtotal);
            
            // Sumamos al total general
            totalVenta += subtotal;
            
            // Importante: vincular el detalle con la venta padre
            detalle.setVenta(venta);
        }

        venta.setTotal(totalVenta);
        return ventaRepository.save(venta);
    }

    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    public Venta buscarPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }
}