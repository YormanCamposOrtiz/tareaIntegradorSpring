package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Compra;
import java.util.List;
import java.util.Optional;

public interface CompraService {
    Compra registrarCompra(Compra compra);
    List<Compra> listarTodas();
    Optional<Compra> buscarPorId(Long id);
}