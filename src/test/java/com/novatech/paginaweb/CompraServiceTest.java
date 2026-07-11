package com.novatech.paginaweb;

import com.novatech.paginaweb.dao.CompraRepository;
import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.model.Compra;
import com.novatech.paginaweb.model.DetalleCompra;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.service.impl.CompraServiceImpl;

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
class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CompraServiceImpl compraService;

    @Test
    void testRegistrarCompraExitosamente() {
        // 1. ARRANGE
        Producto productoOriginal = new Producto();
        productoOriginal.setId(1L);
        productoOriginal.setNombre("Cámara Seguridad HD");
        productoOriginal.setStock(10); // Inventario inicial

        Compra compraDeFront = new Compra();
        compraDeFront.setProveedor("Proveedor Cámaras SAC");

        DetalleCompra detalleEnviado = new DetalleCompra();
        detalleEnviado.setProducto(productoOriginal);
        detalleEnviado.setCantidad(5); // Compramos 5 unidades más
        detalleEnviado.setPrecioCompra(100.0); // Costo unitario acordado

        compraDeFront.setDetalles(Arrays.asList(detalleEnviado));

        // Comportamientos esperados de los mocks
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoOriginal));
        when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 2. ACT
        Compra resultado = compraService.registrarCompra(compraDeFront);

        // 3. ASSERT
        assertNotNull(resultado, "La compra procesada no debería ser nula");
        assertEquals(500.0, resultado.getTotal(), "El total de la compra debe ser 100.0 * 5 = 500.0");
        assertEquals(15, productoOriginal.getStock(), "El stock físico debió aumentar de 10 a 15");
        assertEquals("Proveedor Cámaras SAC", resultado.getProveedor());

        // Validar el detalle consolidado en el backend
        assertEquals(1, resultado.getDetalles().size());
        DetalleCompra detProcesado = resultado.getDetalles().get(0);
        assertEquals(500.0, detProcesado.getSubtotal());
        assertEquals(100.0, detProcesado.getPrecioCompra());

        // Verificar ejecuciones correctas en repositorios
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(productoOriginal);
        verify(compraRepository, times(1)).save(any(Compra.class));
    }

    @Test
    void testRegistrarCompraFallaSinDetalles() {
        // 1. ARRANGE
        Compra compraVacia = new Compra();
        compraVacia.setDetalles(new ArrayList<>()); // Carrito vacío desde React

        // 2. ACT & 3. ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            compraService.registrarCompra(compraVacia);
        });

        assertEquals("La compra debe tener al menos un detalle.", excepcion.getMessage());
        verify(compraRepository, never()).save(any(Compra.class));
    }

    @Test
    void testRegistrarCompraFallaProductoNoExiste() {
        // 1. ARRANGE
        Producto productoInexistente = new Producto();
        productoInexistente.setId(999L);

        Compra compraDeFront = new Compra();
        DetalleCompra detalleEnviado = new DetalleCompra();
        detalleEnviado.setProducto(productoInexistente);
        detalleEnviado.setCantidad(2);
        detalleEnviado.setPrecioCompra(50.0);
        compraDeFront.setDetalles(Arrays.asList(detalleEnviado));

        // Forzamos al repositorio a retornar vacío
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        // 2. ACT & 3. ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            compraService.registrarCompra(compraDeFront);
        });

        assertTrue(excepcion.getMessage().contains("Producto no encontrado con el ID: 999"));
        
        // Regla de oro: Si falla la búsqueda, jamás se guarda nada en base de datos
        verify(productoRepository, never()).save(any(Producto.class));
        verify(compraRepository, never()).save(any(Compra.class));
    }

    @Test
    void testEliminarCompraExitosamenteYRestarStock() {
        // 1. ARRANGE
        Long compraId = 10L;

        Producto productoAsociado = new Producto();
        productoAsociado.setId(2L);
        productoAsociado.setNombre("Router Balanceador");
        productoAsociado.setStock(8); // Stock que figura actualmente en el almacén

        Compra compraExistente = new Compra();
        compraExistente.setId(compraId);

        DetalleCompra detalle = new DetalleCompra();
        detalle.setProducto(productoAsociado);
        detalle.setCantidad(3); // Se habían comprado 3 unidades en esta factura
        
        List<DetalleCompra> detalles = new ArrayList<>();
        detalles.add(detalle);
        compraExistente.setDetalles(detalles);

        when(compraRepository.findById(compraId)).thenReturn(Optional.of(compraExistente));

        // 2. ACT
        compraService.eliminarCompra(compraId);

        // 3. ASSERT
        assertEquals(5, productoAsociado.getStock(), "El stock debió revertirse restando las 3 unidades ingresadas (8 - 3 = 5)");
        
        // Verificar que se haya sincronizado el producto y destruido la compra
        verify(productoRepository, times(1)).save(productoAsociado);
        verify(compraRepository, times(1)).delete(compraExistente);
    }
}