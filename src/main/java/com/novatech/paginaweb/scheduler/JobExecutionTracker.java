package com.novatech.paginaweb.scheduler;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JobExecutionTracker {

    public record JobStatus(
            String nombre,
            String tipo,
            String expresion,
            LocalDateTime ultimaEjecucion,
            String ultimoResultado,
            long ultimoConteo
    ) {}

    private final Map<String, JobStatus> jobs = new ConcurrentHashMap<>();

    public void registrar(String nombre, String tipo, String expresion, String resultado, long conteo) {
        jobs.put(nombre, new JobStatus(
                nombre,
                tipo,
                expresion,
                LocalDateTime.now(),
                resultado,
                conteo
        ));
    }

    public Map<String, JobStatus> obtenerEstados() {
        return new LinkedHashMap<>(jobs);
    }
}
