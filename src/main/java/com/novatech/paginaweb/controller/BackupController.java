package com.novatech.paginaweb.controller;

import com.novatech.paginaweb.service.DatabaseBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    @Autowired
    private DatabaseBackupService backupService;

    @PostMapping("/now")
    public ResponseEntity<String> triggerBackupNow() {
        new Thread(backupService::performBackup).start(); // Ejecuta en segundo plano
        return ResponseEntity.ok("✅ Backup iniciado. Revisa la consola / logs.");
    }
}