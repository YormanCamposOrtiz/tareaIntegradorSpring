package com.novatech.paginaweb.scheduler;

import com.novatech.paginaweb.dao.PedidoRepository;
import com.novatech.paginaweb.metrics.BusinessMetrics;
import com.novatech.paginaweb.model.Pedido;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class CancelarPedidosPendientesJob {

    private static final Logger log = LoggerFactory.getLogger(CancelarPedidosPendientesJob.class);

    private final PedidoRepository pedidoRepository;
    private final BusinessMetrics businessMetrics;
    private final JobExecutionTracker jobExecutionTracker;

    @Value("${app.jobs.pedidos-pendientes.cron:0 0 * * * *}")
    private String cronExpression;

    @Value("${app.jobs.pedidos-pendientes.horas-limite:48}")
    private int horasLimite;

    public CancelarPedidosPendientesJob(
            PedidoRepository pedidoRepository,
            BusinessMetrics businessMetrics,
            JobExecutionTracker jobExecutionTracker
    ) {
        this.pedidoRepository = pedidoRepository;
        this.businessMetrics = businessMetrics;
        this.jobExecutionTracker = jobExecutionTracker;
    }

    @Scheduled(cron = "${app.jobs.pedidos-pendientes.cron:0 0 * * * *}")
    @Transactional
    public void cancelarPedidosAntiguos() {
        LocalDateTime limite = LocalDateTime.now().minusHours(horasLimite);
        List<Pedido> pedidos = pedidoRepository.findByEstadoAndFechaBefore("PENDIENTE", limite);

        int cancelados = 0;
        for (Pedido pedido : pedidos) {
            pedidoRepository.cancelarPedidoProcedimiento(pedido.getId());
            cancelados++;
        }

        businessMetrics.registrarEjecucionPedidosPendientes();

        String resultado = cancelados == 0
                ? "No hay pedidos PENDIENTE mayores a " + horasLimite + " horas"
                : "Se cancelaron " + cancelados + " pedidos PENDIENTE antiguos";

        jobExecutionTracker.registrar(
                "cancelar-pedidos-pendientes",
                "cron",
                cronExpression,
                resultado,
                cancelados
        );

        log.info("[CRON JOB] {}", resultado);
    }
}
