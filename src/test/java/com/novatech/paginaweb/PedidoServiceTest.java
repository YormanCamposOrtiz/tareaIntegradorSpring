package com.novatech.paginaweb;

import com.novatech.paginaweb.dao.PedidoRepository;
import com.novatech.paginaweb.model.DetallePedido;
import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.model.Usuario;
import com.novatech.paginaweb.service.impl.PedidoServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @InjectMocks
    private PedidoServiceImpl pedidoService;

    // --- PRUEBA DE CREACIÓN DE PEDIDO  ---

    @Test
    void testCrearPedidoExitosamenteLlamandoProcedimiento() {
        // 1. ARRANGE
        Usuario usuario = new Usuario();
        usuario.setId(10L);

        Producto producto = new Producto();
        producto.setId(50L);

        DetallePedido detalle = new DetallePedido();
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        Pedido pedidoInput = new Pedido();
        pedidoInput.setUsuario(usuario);
        pedidoInput.setDireccionEnvio("Av. Universitaria, Comas");
        pedidoInput.setObservaciones("Dejar en recepción");
        pedidoInput.getDetalles().add(detalle);

        // Simulamos que la función 'procesar_registro_pedido' de Postgres nos devuelve el ID generado 999
        when(pedidoRepository.registrarPedidoProcedimiento(
                eq(10L),
                eq("Av. Universitaria, Comas"),
                eq("Dejar en recepción"),
                any(Long[].class),
                any(Integer[].class)
        )).thenReturn(999L);

        // Simulamos la recuperación del pedido completo persistido por el procedimiento
        Pedido pedidoPersistido = new Pedido();
        pedidoPersistido.setId(999L);
        pedidoPersistido.setTotal(150.0);
        pedidoPersistido.setEstado("PENDIENTE");

        when(pedidoRepository.findById(999L)).thenReturn(Optional.of(pedidoPersistido));

        // 2. ACT
        Pedido resultado = pedidoService.crearPedido(pedidoInput);

        // 3. ASSERT
        assertNotNull(resultado);
        assertEquals(999L, resultado.getId());
        assertEquals("PENDIENTE", resultado.getEstado());

        // Verificamos que se ejecutó la consulta nativa y el findById de sincronización
        verify(pedidoRepository, times(1)).registrarPedidoProcedimiento(any(), any(), any(), any(), any());
        verify(pedidoRepository, times(1)).findById(999L);
    }

    @Test
    void testCrearPedidoFallaAlNoEncontrarIdGenerado() {
        // 1. ARRANGE
        Pedido pedidoInput = new Pedido();
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        pedidoInput.setUsuario(usuario);

        // El procedimiento dice que guardó el ID 55, pero luego el findById falla (simulación de inconsistencia)
        when(pedidoRepository.registrarPedidoProcedimiento(any(), any(), any(), any(), any())).thenReturn(55L);
        when(pedidoRepository.findById(55L)).thenReturn(Optional.empty());

        // 2. ACT & 3. ASSERT
        RuntimeException excepcion = assertThrows(RuntimeException.class, () -> {
            pedidoService.crearPedido(pedidoInput);
        });

        assertTrue(excepcion.getMessage().contains("Error al recuperar el pedido generado por la BD"));
    }

    // --- PRUEBAS DE LISTADO Y FILTROS POR FECHAS (Vista Admin / Cliente) ---

    @Test
    void testListarTodosConFiltroDeFechas() {
        // 1. ARRANGE
        LocalDateTime inicio = LocalDateTime.now().minusDays(7);
        LocalDateTime fin = LocalDateTime.now();
        
        List<Pedido> listaMock = List.of(new Pedido(), new Pedido());
        when(pedidoRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin)).thenReturn(listaMock);

        // 2. ACT
        List<Pedido> resultado = pedidoService.listarTodos(inicio, fin);

        // 3. ASSERT
        assertEquals(2, resultado.size());
        verify(pedidoRepository, times(1)).findByFechaBetweenOrderByFechaDesc(inicio, fin);
        verify(pedidoRepository, never()).findAllByOrderByFechaDesc();
    }

    @Test
    void testListarTodosSinFechasDevuelveTodo() {
        // 1. ARRANGE
        List<Pedido> listaCompleta = List.of(new Pedido(), new Pedido(), new Pedido());
        when(pedidoRepository.findAllByOrderByFechaDesc()).thenReturn(listaCompleta);

        // 2. ACT
        List<Pedido> resultado = pedidoService.listarTodos(null, null);

        // 3. ASSERT
        assertEquals(3, resultado.size());
        verify(pedidoRepository, times(1)).findAllByOrderByFechaDesc();
        verify(pedidoRepository, never()).findByFechaBetweenOrderByFechaDesc(any(), any());
    }

    // --- PRUEBAS DE ACTUALIZACIÓN Y CANCELACIÓN ---

    @Test
    void testActualizarEstadoExitosamente() {
        // 1. ARRANGE
        Pedido pedidoExistente = new Pedido();
        pedidoExistente.setId(1L);
        pedidoExistente.setEstado("PREPARANDO");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoExistente));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArgument(0));

        // 2. ACT
        Pedido resultado = pedidoService.actualizarEstado(1L, "ENVIADO");

        // 3. ASSERT
        assertNotNull(resultado);
        assertEquals("ENVIADO", resultado.getEstado());
        verify(pedidoRepository, times(1)).save(pedidoExistente);
    }

    @Test
    void testCancelarPedidoLlamandoProcedimiento() {
        // 1. ARRANGE
        Pedido pedidoExistente = new Pedido();
        pedidoExistente.setId(5L);

        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedidoExistente));
        // Al ser un método void en la consulta nativa, no necesitamos configurar 'when(...).thenReturn(...)'

        // 2. ACT
        assertDoesNotThrow(() -> pedidoService.cancelarPedido(5L));

        // 3. ASSERT
        verify(pedidoRepository, times(1)).findById(5L);
        verify(pedidoRepository, times(1)).cancelarPedidoProcedimiento(5L);
    }
}