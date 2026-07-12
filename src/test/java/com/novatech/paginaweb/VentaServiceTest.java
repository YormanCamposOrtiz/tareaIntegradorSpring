package com.novatech.paginaweb;

import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.dao.VentaRepository;
import com.novatech.paginaweb.model.DetalleVenta;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.model.Venta;
import com.novatech.paginaweb.service.impl.VentaServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private VentaServiceImpl ventaService;

    @Test
    void testRegistrarVentaExitosamente() {
        // 1. ARRANGE
        Producto productoOriginal = new Producto();
        productoOriginal.setId(10L);
        productoOriginal.setNombre("Cámara Seguridad HD");
        productoOriginal.setStock(5);
        productoOriginal.setPrecio_venta(150.0);

        Venta ventaDeFront = new Venta();
        DetalleVenta detalleEnviado = new DetalleVenta();
        detalleEnviado.setProducto(productoOriginal);
        detalleEnviado.setCantidad(2); // Queremos llevar 2 unidades
        ventaDeFront.setDetalles(Arrays.asList(detalleEnviado));

        // Simulamos comportamiento de los repositorios
        when(productoRepository.findById(10L)).thenReturn(Optional.of(productoOriginal));
        
        // Simular el guardado final (retornando la misma entidad procesada simulando la BD)
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. ACT
        Venta resultado = ventaService.registrarVenta(ventaDeFront);

        // 3. ASSERT
        assertNotNull(resultado, "La venta guardada no debería ser nula");
        assertEquals(300.0, resultado.getTotal(), "El total calculado debe ser 150.0 * 2 = 300.0");
        assertEquals(3, productoOriginal.getStock(), "El stock debió bajar de 5 a 3");
        
        // Verificar que los detalles se mapearon correctamente en el Backend
        assertEquals(1, resultado.getDetalles().size());
        DetalleVenta detalleProcesado = resultado.getDetalles().get(0);
        assertEquals(300.0, detalleProcesado.getSubtotal());
        assertEquals(150.0, detalleProcesado.getPrecioUnitario());

        // Verificar ejecuciones exactas
        verify(productoRepository, times(1)).findById(10L);
        verify(productoRepository, times(1)).save(productoOriginal);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    void testRegistrarVentaFallaPorStockInsuficiente() {
        // 1. ARRANGE
        Producto productoOriginal = new Producto();
        productoOriginal.setId(11L);
        productoOriginal.setNombre("Router Balanceador");
        productoOriginal.setStock(1); // Solo queda 1 unidad
        productoOriginal.setPrecio_venta(200.0);

        Venta ventaDeFront = new Venta();
        DetalleVenta detalleEnviado = new DetalleVenta();
        detalleEnviado.setProducto(productoOriginal);
        detalleEnviado.setCantidad(3); // Se solicitan 3 unidades (insuficiente)
        ventaDeFront.setDetalles(Arrays.asList(detalleEnviado));

        when(productoRepository.findById(11L)).thenReturn(Optional.of(productoOriginal));

        // 2. ACT & 3. ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            ventaService.registrarVenta(ventaDeFront);
        });

        assertTrue(excepcion.getMessage().contains("Stock insuficiente"));
        
        // El stock NO debió alterarse
        assertEquals(1, productoOriginal.getStock());
        
        // Verificaciones críticas: No se debió guardar ni el producto ni la venta
        verify(productoRepository, never()).save(any(Producto.class));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testRegistrarVentaFallaProductoNoExiste() {
        // 1. ARRANGE
        Producto productoInexistente = new Producto();
        productoInexistente.setId(99L);

        Venta ventaDeFront = new Venta();
        DetalleVenta detalleEnviado = new DetalleVenta();
        detalleEnviado.setProducto(productoInexistente);
        detalleEnviado.setCantidad(1);
        ventaDeFront.setDetalles(Arrays.asList(detalleEnviado));

        // Simulamos que la BD devuelve vacío
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        // 2. ACT & 3. ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            ventaService.registrarVenta(ventaDeFront);
        });

        assertTrue(excepcion.getMessage().contains("Producto no encontrado con el ID: 99"));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void testEliminarVentaExitosamenteYRestaurarStock() {
        // 1. ARRANGE
        Long ventaId = 1L;

        Producto productoAsociado = new Producto();
        productoAsociado.setId(5L);
        productoAsociado.setNombre("Cámara Seguridad HD");
        productoAsociado.setStock(2); // Stock actual en BD

        Venta ventaExistente = new Venta();
        ventaExistente.setId(ventaId);

        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(productoAsociado);
        detalle.setCantidad(3); // Se vendieron 3 originalmente
        
        List<DetalleVenta> detalles = new ArrayList<>();
        detalles.add(detalle);
        ventaExistente.setDetalles(detalles);

        when(ventaRepository.findById(ventaId)).thenReturn(Optional.of(ventaExistente));

        // 2. ACT
        ventaService.eliminarVenta(ventaId);

        // 3. ASSERT
        assertEquals(5, productoAsociado.getStock(), "El stock debió restaurarse sumando las 3 unidades de la venta (2 + 3 = 5)");
        
        // Verificar llamadas requeridas para completar la lógica
        verify(productoRepository, times(1)).save(productoAsociado);
        verify(ventaRepository, times(1)).delete(ventaExistente);
    }
}
