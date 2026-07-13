package com.novatech.paginaweb.scheduler;

import com.novatech.paginaweb.dao.PedidoRepository;
import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.metrics.BusinessMetrics;
import com.novatech.paginaweb.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ActualizarMetricasJob {

    private static final Logger log = LoggerFactory.getLogger(ActualizarMetricasJob.class);

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final BusinessMetrics businessMetrics;
    private final JobExecutionTracker jobExecutionTracker;

    @Value("${app.jobs.metricas.fixed-rate-ms:60000}")
    private long fixedRateMs;

    public ActualizarMetricasJob(
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            BusinessMetrics businessMetrics,
            JobExecutionTracker jobExecutionTracker
    ) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.businessMetrics = businessMetrics;
        this.jobExecutionTracker = jobExecutionTracker;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void inicializarMetricasAlArranque() {
        actualizarMetricas();
    }

    @Scheduled(fixedRateString = "${app.jobs.metricas.fixed-rate-ms:60000}")
    public void actualizarMetricas() {
        int stockBajo = productoRepository.findProductosConStockBajo().size();
        int totalProductos = (int) productoRepository.count();
        int pedidosPendientes = (int) pedidoRepository.findAll().stream()
                .filter(p -> "PENDIENTE".equals(p.getEstado()))
                .count();

        businessMetrics.actualizarInventario(stockBajo, pedidosPendientes, totalProductos);
        businessMetrics.registrarEjecucionMetricas();

        String resultado = "Metricas actualizadas: stockBajo=" + stockBajo
                + ", pendientes=" + pedidosPendientes
                + ", productos=" + totalProductos;

        jobExecutionTracker.registrar(
                "actualizar-metricas",
                "fixed-rate",
                "cada " + fixedRateMs + " ms",
                resultado,
                totalProductos
        );

        log.info("[JOB fixed-rate] {}", resultado);
    }
}
