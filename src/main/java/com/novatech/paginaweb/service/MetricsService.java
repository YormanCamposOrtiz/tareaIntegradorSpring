// src/main/java/com/novatech/paginaweb/service/MetricsService.java
package com.novatech.paginaweb.service;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MetricsService {

    private final MeterRegistry registry;

    private final AtomicInteger stockBajoCount = new AtomicInteger(0);
    private final AtomicInteger totalClientes = new AtomicInteger(0);
    private final AtomicInteger ventasHoy = new AtomicInteger(0);
    private final AtomicInteger totalProductos = new AtomicInteger(0);

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;

        // Registrar los gauges (se actualizan en tiempo real)
        Gauge.builder("app.inventory.low_stock_count", stockBajoCount, AtomicInteger::get)
                .description("Cantidad de productos con stock bajo")
                .register(registry);

        Gauge.builder("app.customers.total", totalClientes, AtomicInteger::get)
                .description("Total de clientes registrados")
                .register(registry);

        Gauge.builder("app.sales.today", ventasHoy, AtomicInteger::get)
                .description("Ventas del día (monto)")
                .register(registry);

        Gauge.builder("app.products.total", totalProductos, AtomicInteger::get)
                .description("Total de productos en el catálogo")
                .register(registry);
    }

    // Métodos para actualizar desde otros servicios
    public void updateStockBajo(int count) {
        stockBajoCount.set(count);
    }

    public void updateTotalClientes(int count) {
        totalClientes.set(count);
    }

    public void updateVentasHoy(double monto) {
        ventasHoy.set((int) monto); // redondeamos a entero
    }

    public void updateTotalProductos(int count) {
        totalProductos.set(count);
    }
}