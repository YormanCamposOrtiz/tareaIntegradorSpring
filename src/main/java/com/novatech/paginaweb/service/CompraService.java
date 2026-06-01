package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Compra;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompraService {
    Compra registrarCompra(Compra compra);
    List<Compra> listarTodas();
    void eliminarCompra(Long id);
    Optional<Compra> buscarPorId(Long id);
    List<Compra> listarPorFechas(LocalDateTime inicio, LocalDateTime fin);
}