package com.novatech.paginaweb.service.impl;

import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.dao.VentaRepository;
import com.novatech.paginaweb.model.DetalleVenta;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.model.Venta;
import com.novatech.paginaweb.service.VentaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Override
    @Transactional // Si una actualización de stock falla, se aplica rollback en toda la transacción de Neon
    public Venta registrarVenta(Venta ventaDeFront) {
        
        // 1. Crear una nueva instancia limpia para asegurar consistencia e integridad
        Venta nuevaVenta = new Venta();
        nuevaVenta.setUsuario(ventaDeFront.getUsuario());
        nuevaVenta.setFecha(LocalDateTime.now());
        
        double totalCalculado = 0.0;
        List<DetalleVenta> listaDetallesConsolidados = new ArrayList<>();

        // 2. Procesar y validar cada detalle enviado desde React
        for (DetalleVenta det : ventaDeFront.getDetalles()) {
            
            // Validar que el producto tenga un ID válido
            if (det.getProducto() == null || det.getProducto().getId() == null) {
                throw new RuntimeException("Debe especificar un ID válido para el producto.");
            }

            // Buscar el producto en tiempo real en la BD de Neon
            Producto producto = productoRepository.findById(det.getProducto().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado con el ID: " + det.getProducto().getId()));

            // Control estricto de Stock antes de vender
            if (producto.getStock() < det.getCantidad()) {
                throw new RuntimeException("Stock insuficiente para: " + producto.getNombre() 
                        + " (Disponible: " + producto.getStock() + " u. | Solicitado: " + det.getCantidad() + " u.)");
            }

            // DESCONTAR EL STOCK FISICO EN ALMACÉN
            producto.setStock(producto.getStock() - det.getCantidad());
            productoRepository.save(producto);

            // Construir el objeto Detalle de forma segura mapeando datos reales del backend
            DetalleVenta nuevoDetalle = new DetalleVenta();
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(det.getCantidad());
            nuevoDetalle.setPrecioUnitario(producto.getPrecio_venta());
            
            double subtotalItem = producto.getPrecio_venta() * det.getCantidad();
            nuevoDetalle.setSubtotal(subtotalItem);
            
            totalCalculado += subtotalItem;

            // ESTABLECER RELACIÓN BIDIRECCIONAL IMPRESCINDIBLE PARA HIBERNATE
            nuevoDetalle.setVenta(nuevaVenta);
            listaDetallesConsolidados.add(nuevoDetalle);
        }

        // 3. Asignar los valores globales calculados a la cabecera
        nuevaVenta.setTotal(totalCalculado);
        nuevaVenta.setDetalles(listaDetallesConsolidados);

        // 4. Guardar en cascada de forma sincronizada
        return ventaRepository.save(nuevaVenta);
    }

    @Override
    @Transactional // Vital para que si falla la reposición de stock, no se borre nada
    public void eliminarVenta(Long id) {
        // 1. Buscar la venta real con sus detalles
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La venta no existe con el ID: " + id));

        // 2. Devolver el stock a los productos antes de eliminar los registros
        for (DetalleVenta det : venta.getDetalles()) {
            Producto producto = det.getProducto();
            if (producto != null) {
                producto.setStock(producto.getStock() + det.getCantidad());
                productoRepository.save(producto);
            }
        }

        // 3. Eliminar la venta (al tener CascadeType.ALL, borrará sus DetalleVenta automáticamente)
        ventaRepository.delete(venta);
    }

    @Override
    public Double obtenerVentasHoy() {

        LocalDate hoy = LocalDate.now();

        LocalDateTime inicio = hoy.atStartOfDay();
        LocalDateTime fin = hoy.plusDays(1).atStartOfDay();

        return ventaRepository.obtenerVentasHoy(inicio, fin);
    }

    @Override
    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta buscarPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public List<Venta> listarPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaBetween(inicio, fin);
    }
}