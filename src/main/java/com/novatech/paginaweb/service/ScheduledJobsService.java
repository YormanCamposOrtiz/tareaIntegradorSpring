package com.novatech.paginaweb.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.novatech.paginaweb.dao.ProductoRepository;
import com.novatech.paginaweb.dao.UsuarioRepository;
import com.novatech.paginaweb.model.Producto;

@Service
public class ScheduledJobsService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobsService.class);

    private static final String JOB_STOCK_ALERTA = "stock-alerta";

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private MetricsService metricsService;        // ← NUEVO

    @Autowired
    private JobMetricsService jobMetricsService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.jobs.stock-alerta.admin-email:mediexpress946@gmail.com}")
    private String adminEmail;

    /**
     * Revisa productos con stock bajo
     */
    @Scheduled(cron = "${app.jobs.stock-alerta.cron:0 0 * * * *}")
    public void verificarStockBajo() {
        ejecutarJob(JOB_STOCK_ALERTA, () -> {
            List<Producto> productosBajoStock = productoRepository.findProductosConStockBajo();

            // Actualizar métrica
            metricsService.updateStockBajo(productosBajoStock.size());

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
     * Actualiza métricas generales cada 5 minutos
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void actualizarMetricasGenerales() {
        ejecutarJob("metricas-generales", () -> {
            // Total de clientes
            long clientes = usuarioRepository.count();
            metricsService.updateTotalClientes((int) clientes);

            // Total de productos
            long productos = productoRepository.count();
            metricsService.updateTotalProductos((int) productos);

            // Ventas del día
            Double ventas = ventaService.obtenerVentasHoy();
            metricsService.updateVentasHoy(ventas != null ? ventas : 0.0);

            log.info("Métricas generales actualizadas → Clientes: {}, Productos: {}, Ventas Hoy: {}",
                    clientes, productos, ventas);
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
            log.warn("[{}] JavaMailSender no disponible.", JOB_STOCK_ALERTA);
            return;
        }

        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setTo(adminEmail);
            mensaje.setSubject("Alerta de stock bajo - NovaTech");
            mensaje.setText(detalle);
            mailSender.send(mensaje);
            log.info("[{}] Correo de alerta enviado.", JOB_STOCK_ALERTA);
        } catch (Exception e) {
            log.error("[{}] Error enviando correo: {}", JOB_STOCK_ALERTA, e.getMessage());
        }
    }
}