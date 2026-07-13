package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.scheduler.JobExecutionTracker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
public class JobStatusController {

    @Autowired
    private JobExecutionTracker jobExecutionTracker;

    @GetMapping("/status")
    public Map<String, JobExecutionTracker.JobStatus> obtenerEstadoJobs() {
        return jobExecutionTracker.obtenerEstados();
    }
}
