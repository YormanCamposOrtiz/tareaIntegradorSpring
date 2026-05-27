package com.novatech.paginaweb.service.impl;

import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.dao.PedidoRepository;
import com.novatech.paginaweb.model.DetallePedido;
import com.novatech.paginaweb.service.PedidoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override
    @Transactional
    public Pedido crearPedido(Pedido pedido) {
        // 1. Establecer el estado inicial
        pedido.setEstado("PREPARANDO");

        // 2. Vincular detalles y calcular el total general
        double totalAcumulado = 0;
        if (pedido.getDetalles() != null) {
            for (DetallePedido detalle : pedido.getDetalles()) {
                detalle.setPedido(pedido); // Relación bidireccional
                
                // Calculamos subtotal por seguridad
                double subtotal = detalle.getCantidad() * detalle.getPrecioUnitario();
                detalle.setSubtotal(subtotal);
                totalAcumulado += subtotal;
            }
        }
        
        pedido.setTotal(totalAcumulado);
        return pedidoRepository.save(pedido);
    }

    @Override
    public List<Pedido> listarTodos() {
        // Listar todos los pedidos (para el Admin)
        return pedidoRepository.findAll();
    }

    @Override
    public List<Pedido> listarPorCliente(Long clienteId) {
        // Listar pedidos de un cliente específico (para el perfil del Cliente en React)
        return pedidoRepository.findByClienteIdOrderByFechaDesc(clienteId);
    }

    @Override
    public Optional<Pedido> obtenerPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    @Override
    @Transactional
    public Pedido actualizarEstado(Long id, String nuevoEstado) {
        return pedidoRepository.findById(id).map(pedido -> {
            pedido.setEstado(nuevoEstado);
            return pedidoRepository.save(pedido);
        }).orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public void cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el pedido"));
        
        if ("ENTREGADO".equals(pedido.getEstado())) {
            throw new RuntimeException("No se puede cancelar un pedido que ya fue entregado");
        }
        
        pedido.setEstado("CANCELADO");
        pedidoRepository.save(pedido);
    }
}