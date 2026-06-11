package com.novatech.paginaweb;

import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.service.impl.ProductoServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Cambiado para pruebas unitarias rápidas
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Test
    void testListarProductosCorrectamente() {
        // 1. ARRANGED
        Producto prod1 = new Producto();
        prod1.setId(1L);
        prod1.setNombre("Cámara Seguridad HD");

        Producto prod2 = new Producto();
        prod2.setId(2L);
        prod2.setNombre("Router Balanceador");

        when(productoRepository.findAll()).thenReturn(Arrays.asList(prod1, prod2));

        // 2. ACT
        List<Producto> resultado = productoService.listarTodos(); 

        // 3. ASSERT
        assertNotNull(resultado, "La lista no debería retornar nula");
        assertEquals(2, resultado.size(), "La lista debe contener exactamente 2 productos");
        assertEquals("Cámara Seguridad HD", resultado.get(0).getNombre(), "El primer producto debe coincidir");

        verify(productoRepository, times(1)).findAll();
    }
}