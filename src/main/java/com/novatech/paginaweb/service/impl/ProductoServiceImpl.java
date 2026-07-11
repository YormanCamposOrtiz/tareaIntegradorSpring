package com.novatech.paginaweb.service.impl;

import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

   @Override
    public List<Producto> listarVisibles() {
        return productoRepository.findAll().stream()
                .filter(Producto::isVisibilidad)
                .toList();
    }

    @Override
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    public Producto guardar(Producto producto) {
        return productoRepository.save(producto);
    }

    @Override
    public Producto buscarPorId(Long id) {
        return productoRepository.findById(id).orElse(null);
    }

    @Override
    public void eliminar(Long id) {
        Producto producto = buscarPorId(id);
        if (producto != null) {
            producto.setVisibilidad(!producto.isVisibilidad());
            productoRepository.save(producto);
        }
    }

    @Override
    public List<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre);
    }

    @Override
    public long contarProductosStockBajo() {
         return productoRepository.contarStockBajo();
    }

    @Override
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
