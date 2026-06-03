package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Pedido;

import java.util.List;
import java.util.Optional;


public interface PedidoService {

    Pedido crearPedido(Pedido pedido);

    // Listar todos los pedidos (para el Admin)
    List<Pedido> listarTodos();

    // Listar pedidos de un usuario específico (para el perfil del Usuario en React)
    List<Pedido> listarPorUsuario(Long usuarioId);

    Optional<Pedido> obtenerPorId(Long id);


    Pedido actualizarEstado(Long id, String nuevoEstado);

    void cancelarPedido(Long id);
}
