package com.novatech.paginaweb.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.novatech.paginaweb.dao.PedidoRepository;
import com.novatech.paginaweb.model.DetallePedido;
import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.service.PedidoService;

@Service
public class PedidoServiceImpl implements PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Override // 👈 Aseguramos implementar exactamente el método del PedidoService
    @Transactional
    public Pedido crearPedido(Pedido pedido) {
        
        // 1. Extraer los IDs de productos y cantidades directamente en Listas de Integer
        List<Long> productosIds = pedido.getDetalles().stream()
                .map(d -> d.getProducto().getId())
                .collect(Collectors.toList());

        List<Integer> cantidades = pedido.getDetalles().stream()
                .map(DetallePedido::getCantidad)
                .collect(Collectors.toList());

        Long pedidoIdGenerated =
            pedidoRepository.registrarPedidoProcedimiento(
                pedido.getUsuario().getId(),
                pedido.getDireccionEnvio(),
                pedido.getObservaciones(),
                productosIds.toArray(new Long[0]),
                cantidades.toArray(new Integer[0])
            );

        // 3. Recuperar la entidad completa con todas sus relaciones desde la base de datos
        return pedidoRepository.findByIdWithDetalles(pedidoIdGenerated.longValue())
                .orElseThrow(() -> new RuntimeException("Error al recuperar el pedido generado por la BD."));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarTodos(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio != null && fin != null) {
            return pedidoRepository.findByFechaBetweenWithDetalles(inicio, fin);
        }
        return pedidoRepository.findAllWithDetalles();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorUsuario(Long usuarioId, LocalDateTime inicio, LocalDateTime fin) {
        if (inicio != null && fin != null) {
            return pedidoRepository.findByUsuarioIdAndFechaBetweenWithDetalles(usuarioId, inicio, fin);
        }
        return pedidoRepository.findByUsuarioIdWithDetalles(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllWithDetalles();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> listarPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioIdWithDetalles(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Pedido> obtenerPorId(Long id) {
        return pedidoRepository.findByIdWithDetalles(id);
    }

    @Override
    @Transactional
    public Pedido actualizarEstado(Long id, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findByIdWithDetalles(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    @Override
    @Transactional
    public void cancelarPedido(Long id) {

        System.out.println("ENTRE AL SERVICIO CANCELAR PEDIDO");

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró el pedido"));

        pedidoRepository.cancelarPedidoProcedimiento(id);

        System.out.println("SALI DEL PROCEDIMIENTO");
    }
}