package com.novatech.paginaweb.service;

import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> listarVisibles() {
        return productoRepository.findAll().stream()
                .filter(Producto::isVisibilidad)
                .toList();
    }

    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        if (producto != null) {
            producto.setVisibilidad(!producto.isVisibilidad());
            productoRepository.save(producto);
        }
    }

    // Método crucial para el flujo de ventas
    public void reducirStock(Long id, Integer cantidad) {
        Producto producto = buscarPorId(id);
        if (producto != null) {
            if (producto.getStock() >= cantidad) {
                producto.setStock(producto.getStock() - cantidad);
                productoRepository.save(producto);
            } else {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getNombre());
            }
        }
    }
}