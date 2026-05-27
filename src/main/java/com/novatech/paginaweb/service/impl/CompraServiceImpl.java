package com.novatech.paginaweb.service.impl;

import com.novatech.paginaweb.dao.CompraRepository;
import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.model.DetalleCompra;
import com.novatech.paginaweb.service.CompraService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CompraServiceImpl implements CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Override
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

    @Override
    public List<Compra> listarTodas() {
        return compraRepository.findAll();
    }

    @Override
    public Optional<Compra> buscarPorId(Long id) {
        return compraRepository.findById(id);
    }
}