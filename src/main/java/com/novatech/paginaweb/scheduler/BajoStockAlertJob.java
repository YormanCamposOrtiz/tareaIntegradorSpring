package com.novatech.paginaweb.scheduler;

import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.metrics.BusinessMetrics;
import com.novatech.paginaweb.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BajoStockAlertJob {

    private static final Logger log = LoggerFactory.getLogger(BajoStockAlertJob.class);

    private final ProductoRepository productoRepository;
    private final BusinessMetrics businessMetrics;
    private final JobExecutionTracker jobExecutionTracker;

    @Value("${app.jobs.bajo-stock.cron:0 0 8 * * *}")
    private String cronExpression;

    public BajoStockAlertJob(
            ProductoRepository productoRepository,
            BusinessMetrics businessMetrics,
            JobExecutionTracker jobExecutionTracker
    ) {
        this.productoRepository = productoRepository;
        this.businessMetrics = businessMetrics;
        this.jobExecutionTracker = jobExecutionTracker;
    }

    @Scheduled(cron = "${app.jobs.bajo-stock.cron:0 0 8 * * *}")
    public void revisarStockBajo() {
        List<Producto> productos = productoRepository.findProductosConStockBajo();
        businessMetrics.registrarEjecucionBajoStock();

        String nombres = productos.stream()
                .map(Producto::getNombre)
                .collect(Collectors.joining(", "));

        String resultado = productos.isEmpty()
                ? "Sin productos con stock bajo"
                : "Productos con stock bajo: " + nombres;

        jobExecutionTracker.registrar(
                "alerta-stock-bajo",
                "cron",
                cronExpression,
                resultado,
                productos.size()
        );

        log.warn("[CRON JOB] {}", resultado);
    }
}
