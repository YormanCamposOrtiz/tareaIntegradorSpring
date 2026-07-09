package com.novatech.paginaweb.service;

import com.novatech.paginaweb.dao.PedidoRepository;
import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.model.Pedido;
import com.novatech.paginaweb.model.Producto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ScheduledJobsService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobsService.class);

    private static final String JOB_STOCK_ALERTA = "stock-alerta";
    private static final String JOB_PEDIDOS_LIMPIEZA = "pedidos-limpieza";
    private static final String JOB_METRICAS_INVENTARIO = "metricas-inventario";

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private JobMetricsService jobMetricsService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.jobs.stock-alerta.admin-email:mediexpress946@gmail.com}")
    private String adminEmail;

    @Value("${app.jobs.pedidos-limpieza.dias-pendiente:7}")
    private int diasPedidoPendiente;

    /**
     * Revisa productos con stock bajo y registra alertas.
     * Por defecto: cada hora en el minuto 0.
     */
    @Scheduled(cron = "${app.jobs.stock-alerta.cron:0 0 * * * *}")
    public void verificarStockBajo() {
        ejecutarJob(JOB_STOCK_ALERTA, () -> {
            List<Producto> productosBajoStock = productoRepository.findProductosConStockBajo();
            jobMetricsService.updateGauge("app.inventory.low_stock_count", JOB_STOCK_ALERTA, productosBajoStock.size());

            if (productosBajoStock.isEmpty()) {
                log.info("[{}] No hay productos con stock bajo.", JOB_STOCK_ALERTA);
                return;
            }

            StringBuilder detalle = new StringBuilder("Productos con stock bajo:\n");
            for (Producto producto : productosBajoStock) {
                detalle.append("- ")
                        .append(producto.getNombre())
                        .append(" | stock=")
                        .append(producto.getStock())
                        .append(" | minimo=")
                        .append(producto.getStockMin())
                        .append("\n");
            }

            log.warn("[{}] {} producto(s) con stock bajo.\n{}", JOB_STOCK_ALERTA, productosBajoStock.size(), detalle);
            enviarAlertaStock(detalle.toString());
        });
    }

    /**
     * Cancela pedidos PENDIENTE con mas dias de antiguedad configurados.
     * Por defecto: todos los dias a las 2:00 AM.
     */
    @Scheduled(cron = "${app.jobs.pedidos-limpieza.cron:0 0 2 * * *}")
    @Transactional
    public void cancelarPedidosPendientesAntiguos() {
        ejecutarJob(JOB_PEDIDOS_LIMPIEZA, () -> {
            LocalDateTime limite = LocalDateTime.now().minusDays(diasPedidoPendiente);
            List<Pedido> pedidosAntiguos = pedidoRepository.findByEstadoAndFechaBefore("PENDIENTE", limite);

            jobMetricsService.updateGauge("app.orders.pending_expired_count", JOB_PEDIDOS_LIMPIEZA, pedidosAntiguos.size());

            for (Pedido pedido : pedidosAntiguos) {
                pedidoRepository.cancelarPedidoProcedimiento(pedido.getId());
                log.info("[{}] Pedido #{} cancelado por antiguedad (fecha: {}).",
                        JOB_PEDIDOS_LIMPIEZA, pedido.getId(), pedido.getFecha());
            }

            log.info("[{}] Proceso finalizado. Pedidos cancelados: {}", JOB_PEDIDOS_LIMPIEZA, pedidosAntiguos.size());
        });
    }

    /**
     * Publica metricas de inventario y pedidos para Grafana.
     * Por defecto: cada 5 minutos.
     */
    @Scheduled(cron = "${app.jobs.metricas-inventario.cron:0 */5 * * * *}")
    public void actualizarMetricasInventario() {
        ejecutarJob(JOB_METRICAS_INVENTARIO, () -> {
            long totalProductos = productoRepository.count();
            long productosVisibles = productoRepository.countByVisibilidadTrue();
            long productosBajoStock = productoRepository.findProductosConStockBajo().size();
            long pedidosPendientes = pedidoRepository.countByEstado("PENDIENTE");

            jobMetricsService.updateGauge("app.inventory.total_products", JOB_METRICAS_INVENTARIO, (int) totalProductos);
            jobMetricsService.updateGauge("app.inventory.visible_products", JOB_METRICAS_INVENTARIO, (int) productosVisibles);
            jobMetricsService.updateGauge("app.inventory.low_stock_count", JOB_METRICAS_INVENTARIO, (int) productosBajoStock);
            jobMetricsService.updateGauge("app.orders.pending_count", JOB_METRICAS_INVENTARIO, (int) pedidosPendientes);

            log.debug("[{}] Metricas actualizadas: productos={}, visibles={}, bajoStock={}, pedidosPendientes={}",
                    JOB_METRICAS_INVENTARIO, totalProductos, productosVisibles, productosBajoStock, pedidosPendientes);
        });
    }

    private void ejecutarJob(String jobName, Runnable task) {
        long inicio = System.currentTimeMillis();
        boolean exito = true;
        try {
            task.run();
        } catch (Exception e) {
            exito = false;
            log.error("[{}] Error ejecutando job: {}", jobName, e.getMessage(), e);
        } finally {
            jobMetricsService.recordJob(jobName, System.currentTimeMillis() - inicio, exito);
        }
    }

    private void enviarAlertaStock(String detalle) {
        if (mailSender == null) {
            log.warn("[{}] JavaMailSender no disponible. Alerta solo registrada en logs.", JOB_STOCK_ALERTA);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(adminEmail);
            mensaje.setSubject("Alerta de stock bajo - NovaTech");
            mensaje.setText(detalle);
            mailSender.send(mensaje);
            log.info("[{}] Correo de alerta enviado a {}", JOB_STOCK_ALERTA, adminEmail);
        } catch (Exception e) {
            log.error("[{}] No se pudo enviar el correo de alerta: {}", JOB_STOCK_ALERTA, e.getMessage());
        }
    }
}
