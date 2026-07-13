package com.novatech.paginaweb.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class BusinessMetrics {

    private final AtomicInteger productosStockBajo = new AtomicInteger(0);
    private final AtomicInteger pedidosPendientes = new AtomicInteger(0);
    private final AtomicInteger totalProductos = new AtomicInteger(0);

    private final Counter bajoStockJobEjecuciones;
    private final Counter pedidosPendientesJobEjecuciones;
    private final Counter metricasJobEjecuciones;

    public BusinessMetrics(MeterRegistry registry) {
        Gauge.builder("paginaweb.productos.stock_bajo", productosStockBajo, AtomicInteger::get)
                .description("Cantidad de productos visibles con stock bajo o igual al minimo")
                .register(registry);

        Gauge.builder("paginaweb.pedidos.pendientes", pedidosPendientes, AtomicInteger::get)
                .description("Cantidad de pedidos en estado PENDIENTE")
                .register(registry);

        Gauge.builder("paginaweb.productos.total", totalProductos, AtomicInteger::get)
                .description("Cantidad total de productos en inventario")
                .register(registry);

        bajoStockJobEjecuciones = Counter.builder("paginaweb.jobs.bajo_stock.ejecuciones")
                .description("Veces que se ejecuto el cron job de stock bajo")
                .register(registry);

        pedidosPendientesJobEjecuciones = Counter.builder("paginaweb.jobs.pedidos_pendientes.ejecuciones")
                .description("Veces que se ejecuto el cron job de pedidos pendientes")
                .register(registry);

        metricasJobEjecuciones = Counter.builder("paginaweb.jobs.metricas.ejecuciones")
                .description("Veces que se ejecuto el job periodico de metricas")
                .register(registry);
    }

    public void actualizarInventario(int stockBajo, int pendientes, int productos) {
        productosStockBajo.set(stockBajo);
        pedidosPendientes.set(pendientes);
        totalProductos.set(productos);
    }

    public void registrarEjecucionBajoStock() {
        bajoStockJobEjecuciones.increment();
    }

    public void registrarEjecucionPedidosPendientes() {
        pedidosPendientesJobEjecuciones.increment();
    }

    public void registrarEjecucionMetricas() {
        metricasJobEjecuciones.increment();
    }
}
