package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Pedido;

import java.util.List;
import java.util.Optional;


public interface PedidoService {

    Pedido crearPedido(Pedido pedido);

    // Listar todos los pedidos (para el Admin)
    List<Pedido> listarTodos();

    // Listar pedidos de un cliente específico (para el perfil del Cliente en React)
    List<Pedido> listarPorCliente(Long clienteId);

    Optional<Pedido> obtenerPorId(Long id);


    Pedido actualizarEstado(Long id, String nuevoEstado);

    void cancelarPedido(Long id);
}
