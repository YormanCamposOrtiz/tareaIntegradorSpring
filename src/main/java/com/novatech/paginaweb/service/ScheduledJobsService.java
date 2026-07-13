package com.novatech.paginaweb.service;

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
import com.novatech.paginaweb.model.Producto;

@Service
public class ScheduledJobsService {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobsService.class);

    private static final String JOB_STOCK_ALERTA = "stock-alerta";

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private JobMetricsService jobMetricsService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.jobs.stock-alerta.admin-email:mediexpress946@gmail.com}")
    private String adminEmail;

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
