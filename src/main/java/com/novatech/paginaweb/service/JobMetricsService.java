package com.novatech.paginaweb.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class JobMetricsService {

    private final MeterRegistry registry;
    private final Map<String, AtomicInteger> gauges = new ConcurrentHashMap<>();

    public JobMetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordJob(String jobName, long durationMs, boolean success) {
        Timer.builder("app.cron.job.execution")
                .tag("job", jobName)
                .tag("status", success ? "success" : "failure")
                .register(registry)
                .record(durationMs, TimeUnit.MILLISECONDS);

        Counter.builder("app.cron.job.runs")
                .tag("job", jobName)
                .tag("status", success ? "success" : "failure")
                .register(registry)
                .increment();
    }

    public void updateGauge(String metricName, String jobName, int value) {
        String key = metricName + ":" + jobName;
        AtomicInteger holder = gauges.computeIfAbsent(key, k -> {
            AtomicInteger atomic = new AtomicInteger(0);
            Gauge.builder(metricName, atomic, AtomicInteger::get)
                    .tag("job", jobName)
                    .register(registry);
            return atomic;
        });
        holder.set(value);
    }
}