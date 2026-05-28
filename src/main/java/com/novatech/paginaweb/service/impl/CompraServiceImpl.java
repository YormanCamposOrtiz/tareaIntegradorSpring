package com.novatech.paginaweb.service.impl;

import com.novatech.paginaweb.dao.CompraRepository;
import com.novatech.paginaweb.dao.ProductoRepository; // <-- Importante incluirlo
import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.model.DetalleCompra;
import com.novatech.paginaweb.model.Producto;         // <-- Importante incluirlo
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

    @Autowired
    private ProductoRepository productoRepository; // <-- Inyectamos para alterar el stock

    @Override
    @Transactional // Mantiene la operación segura; si un producto falla, no se guarda nada
    public Compra registrarCompra(Compra compra) {
        // 1. Validar que la compra tenga detalles
        if (compra.getDetalles() == null || compra.getDetalles().isEmpty()) {
            throw new RuntimeException("La compra debe tener al menos un detalle.");
        }

        double totalCalculado = 0;

        // 2. Procesar cada artículo del detalle
        for (DetalleCompra detalle : compra.getDetalles()) {
            // Buscar el producto real en la base de datos
            Producto producto = productoRepository.findById(detalle.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalle.getProducto().getId()));

            // AUMENTAR el stock en el almacén
            producto.setStock(producto.getStock() + detalle.getCantidad());
            productoRepository.save(producto); // Sincroniza el nuevo stock en la BD

            // Vincular con la cabecera (Relación Bidireccional)
            detalle.setCompra(compra);

            // Calcular subtotales
            double subtotal = detalle.getCantidad() * detalle.getPrecioCompra();
            detalle.setSubtotal(subtotal);
            totalCalculado += subtotal;
        }

        // 3. Asignar el total final calculado a la cabecera
        compra.setTotal(totalCalculado);

        // 4. Guardar en cascada (gracias a CascadeType.ALL en Compra.java)
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