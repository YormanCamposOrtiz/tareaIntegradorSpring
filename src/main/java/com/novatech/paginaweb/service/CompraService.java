package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.model.DetalleCompra;
import com.novatech.paginaweb.repository.CompraRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Transactional
    public Compra registrarCompra(Compra compra) {
        // 1. Validar que la compra tenga detalles
        if (compra.getDetalles() == null || compra.getDetalles().isEmpty()) {
            throw new RuntimeException("La compra debe tener al menos un detalle.");
        }

        // 2. Vincular cada detalle con la cabecera (Compra) 
        // y calcular subtotales si no vienen del frontend
        double totalCalculado = 0;
        for (DetalleCompra detalle : compra.getDetalles()) {
            detalle.setCompra(compra); // Crucial para la relación Bidireccional
            
            double subtotal = detalle.getCantidad() * detalle.getPrecioCompra();
            detalle.setSubtotal(subtotal);
            totalCalculado += subtotal;
        }

        // 3. Asignar el total calculado a la cabecera
        compra.setTotal(totalCalculado);

        // 4. Guardar (Gracias al CascadeType.ALL se guardan los detalles automáticamente)
        return compraRepository.save(compra);
    }

    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    public Optional<Compra> buscarPorId(Long id) {
        return compraRepository.findById(id);
    }
}
