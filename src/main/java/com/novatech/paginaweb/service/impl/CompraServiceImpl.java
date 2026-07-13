package com.novatech.paginaweb.service.impl;

import com.novatech.paginaweb.dao.CompraRepository;
import com.novatech.paginaweb.dao.ProductoRepository; 
import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.model.DetalleCompra;
import com.novatech.paginaweb.model.Producto;         
import com.novatech.paginaweb.service.CompraService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CompraServiceImpl implements CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    @Transactional // Si la carga de un producto falla, se aplica rollback automático en la BD de Neon
    public Compra registrarCompra(Compra compraDeFront) {
        
        // 1. Validar que la compra contenga artículos antes de procesar
        if (compraDeFront.getDetalles() == null || compraDeFront.getDetalles().isEmpty()) {
            throw new RuntimeException("La compra debe tener al menos un detalle.");
        }

        // Crear una nueva instancia limpia para asegurar consistencia e integridad (Igual que en Ventas)
        Compra nuevaCompra = new Compra();
        nuevaCompra.setUsuario(compraDeFront.getUsuario());
        nuevaCompra.setProveedor(compraDeFront.getProveedor());
        nuevaCompra.setFecha(LocalDateTime.now()); // Setea la fecha y hora del servidor actual
        
        double totalCalculado = 0.0;
        List<DetalleCompra> listaDetallesConsolidados = new ArrayList<>();

        // 2. Procesar y validar cada detalle enviado desde React
        for (DetalleCompra det : compraDeFront.getDetalles()) {
            
            // Validar que el producto tenga un ID válido
            if (det.getProducto() == null || det.getProducto().getId() == null) {
                throw new RuntimeException("Debe especificar un ID válido para el producto.");
            }

            // Buscar el producto en tiempo real en la BD
            Producto producto = productoRepository.findById(det.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con el ID: " + det.getProducto().getId()));

            // ¡LÓGICA DE COMPRA!: AUMENTAR EL STOCK FÍSICO EN ALMACÉN
            producto.setStock(producto.getStock() + det.getCantidad());
            productoRepository.save(producto); // Sincroniza el inventario sumado

            // Construir el objeto DetalleCompra de forma segura (Clon de tu lógica de Ventas)
            DetalleCompra nuevoDetalle = new DetalleCompra();
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(det.getCantidad());
            
            // Usamos el precio que nos dicta el frente/proveedor para este lote
            nuevoDetalle.setPrecioCompra(det.getPrecioCompra());
            
            // Calcular subtotal de este ítem específico
            double subtotalItem = det.getPrecioCompra() * det.getCantidad();
            nuevoDetalle.setSubtotal(subtotalItem);
            
            totalCalculado += subtotalItem;

            // ESTABLECER RELACIÓN BIDIRECCIONAL IMPRESCINDIBLE PARA HIBERNATE
            nuevoDetalle.setCompra(nuevaCompra);
            listaDetallesConsolidados.add(nuevoDetalle);
        }

        // 3. Asignar los valores globales calculados a la cabecera
        nuevaCompra.setTotal(totalCalculado);
        nuevaCompra.setDetalles(listaDetallesConsolidados);

        // 4. Guardar en cascada de forma sincronizada
        return compraRepository.save(nuevaCompra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarTodas() {
        return compraRepository.findAllWithDetalles();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Compra> buscarPorId(Long id) {
        return compraRepository.findByIdWithDetalles(id);
    }
    @Override
    @Transactional
    public void eliminarCompra(Long id) {
        // 1. Buscar la compra con sus detalles
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con el ID: " + id));

        // 2. Revertir el stock restando las cantidades ingresadas
        for (DetalleCompra detalle : compra.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto != null) {
                producto.setStock(producto.getStock() - detalle.getCantidad());
                productoRepository.save(producto);
            }
        }

        // 3. Eliminar de la base de datos (elimina cabecera y detalles en cascada)
        compraRepository.delete(compra);
    }
    @Override
    @Transactional(readOnly = true)
    public List<Compra> listarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return compraRepository.findByFechaBetweenWithDetalles(inicio, fin);
    }
}