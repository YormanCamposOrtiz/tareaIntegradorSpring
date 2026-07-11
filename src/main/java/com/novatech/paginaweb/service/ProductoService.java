package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Producto;

import java.util.List;


public interface ProductoService {

    List<Producto> listarVisibles();

    List<Producto> listarTodos();

    Producto guardar(Producto producto);

    Producto buscarPorId(Long id);

    void eliminar(Long id);

    void reducirStock(Long id, Integer cantidad);

    List<Producto> buscarPorNombre(String nombre);

    long contarProductosStockBajo();
}
