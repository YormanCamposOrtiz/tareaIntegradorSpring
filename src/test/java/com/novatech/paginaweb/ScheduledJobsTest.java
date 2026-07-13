package com.novatech.paginaweb;

import com.novatech.paginaweb.dao.PedidoRepository;
import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.metrics.BusinessMetrics;
import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.model.Producto;
import com.novatech.paginaweb.scheduler.ActualizarMetricasJob;
import com.novatech.paginaweb.scheduler.BajoStockAlertJob;
import com.novatech.paginaweb.scheduler.CancelarPedidosPendientesJob;
import com.novatech.paginaweb.scheduler.JobExecutionTracker;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduledJobsTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    private JobExecutionTracker jobExecutionTracker;
    private BusinessMetrics businessMetrics;

    @BeforeEach
    void setUp() {
        jobExecutionTracker = new JobExecutionTracker();
        businessMetrics = new BusinessMetrics(new SimpleMeterRegistry());
    }

    @Test
    void actualizarMetricasJobRegistraEstado() {
        Producto producto = new Producto();
        producto.setNombre("Aspirina");
        producto.setStock(2);
        producto.setStockMin(5);
        producto.setVisibilidad(true);

        Pedido pedido = new Pedido();
        pedido.setEstado("PENDIENTE");

        when(productoRepository.findProductosConStockBajo()).thenReturn(List.of(producto));
        when(productoRepository.count()).thenReturn(1L);
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));

        ActualizarMetricasJob job = new ActualizarMetricasJob(
                productoRepository,
                pedidoRepository,
                businessMetrics,
                jobExecutionTracker
        );
        ReflectionTestUtils.setField(job, "fixedRateMs", 60000L);

        job.actualizarMetricas();

        Map<String, JobExecutionTracker.JobStatus> estados = jobExecutionTracker.obtenerEstados();
        assertTrue(estados.containsKey("actualizar-metricas"));
        assertEquals("fixed-rate", estados.get("actualizar-metricas").tipo());
        assertEquals(1, estados.get("actualizar-metricas").ultimoConteo());
        assertTrue(estados.get("actualizar-metricas").ultimoResultado().contains("stockBajo=1"));
    }

    @Test
    void bajoStockAlertJobDetectaProductos() {
        Producto producto = new Producto();
        producto.setNombre("Paracetamol");
        producto.setStock(1);
        producto.setStockMin(10);
        producto.setVisibilidad(true);

        when(productoRepository.findProductosConStockBajo()).thenReturn(List.of(producto));

        BajoStockAlertJob job = new BajoStockAlertJob(
                productoRepository,
                businessMetrics,
                jobExecutionTracker
        );
        ReflectionTestUtils.setField(job, "cronExpression", "0 0 8 * * *");

        job.revisarStockBajo();

        JobExecutionTracker.JobStatus estado = jobExecutionTracker.obtenerEstados().get("alerta-stock-bajo");
        assertNotNull(estado);
        assertEquals("cron", estado.tipo());
        assertEquals(1, estado.ultimoConteo());
        assertTrue(estado.ultimoResultado().contains("Paracetamol"));
    }

    @Test
    void cancelarPedidosPendientesJobCancelaAntiguos() {
        Pedido pedido = new Pedido();
        pedido.setId(99L);
        pedido.setEstado("PENDIENTE");
        pedido.setFecha(LocalDateTime.now().minusDays(3));

        when(pedidoRepository.findByEstadoAndFechaBefore(eq("PENDIENTE"), any(LocalDateTime.class)))
                .thenReturn(List.of(pedido));

        CancelarPedidosPendientesJob job = new CancelarPedidosPendientesJob(
                pedidoRepository,
                businessMetrics,
                jobExecutionTracker
        );
        ReflectionTestUtils.setField(job, "cronExpression", "0 0 * * * *");
        ReflectionTestUtils.setField(job, "horasLimite", 48);

        job.cancelarPedidosAntiguos();

        verify(pedidoRepository).cancelarPedidoProcedimiento(99L);

        JobExecutionTracker.JobStatus estado = jobExecutionTracker.obtenerEstados().get("cancelar-pedidos-pendientes");
        assertNotNull(estado);
        assertEquals("cron", estado.tipo());
        assertEquals(1, estado.ultimoConteo());
        assertTrue(estado.ultimoResultado().contains("Se cancelaron 1 pedidos"));
    }

    @Test
    void cancelarPedidosPendientesJobSinPedidosAntiguos() {
        when(pedidoRepository.findByEstadoAndFechaBefore(eq("PENDIENTE"), any(LocalDateTime.class)))
                .thenReturn(List.of());

        CancelarPedidosPendientesJob job = new CancelarPedidosPendientesJob(
                pedidoRepository,
                businessMetrics,
                jobExecutionTracker
        );
        ReflectionTestUtils.setField(job, "cronExpression", "0 0 * * * *");
        ReflectionTestUtils.setField(job, "horasLimite", 48);

        job.cancelarPedidosAntiguos();

        verify(pedidoRepository, never()).cancelarPedidoProcedimiento(anyLong());

        JobExecutionTracker.JobStatus estado = jobExecutionTracker.obtenerEstados().get("cancelar-pedidos-pendientes");
        assertNotNull(estado);
        assertTrue(estado.ultimoResultado().contains("No hay pedidos PENDIENTE"));
    }
}
