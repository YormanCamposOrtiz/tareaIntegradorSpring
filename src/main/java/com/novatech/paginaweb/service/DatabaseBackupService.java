package com.novatech.paginaweb.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class DatabaseBackupService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseBackupService.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private static final String BACKUP_DIR = "backups/";

    public DatabaseBackupService() {
        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));
            log.info("✅ Directorio de backups creado");
        } catch (IOException e) {
            log.error("Error creando directorio de backups", e);
        }
    }

    @Scheduled(cron = "0 0 2 * * *") 
    public void performBackup() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        String fileName = "backup_neon_" + timestamp + ".dump";
        String backupPath = BACKUP_DIR + fileName;

        try {
            log.info("🚀 Iniciando backup: {}", fileName);

            ProcessBuilder pb = new ProcessBuilder(
                "pg_dump", 
                "-h", getHostFromUrl(dbUrl), 
                "-U", dbUser, 
                "-d", getDbNameFromUrl(dbUrl), 
                "-F", "c", 
                "-f", backupPath
            );

            pb.environment().put("PGPASSWORD", dbPassword);
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("✅ Backup creado exitosamente: {}", backupPath);
            } else {
                log.error("❌ pg_dump falló con código: {}", exitCode);
            }
        } catch (Exception e) {
            log.error("❌ Error durante el backup", e);
        }
    }

    private String getHostFromUrl(String url) {
        return url.split("//")[1].split(":")[0].split("/")[0];
    }

    private String getDbNameFromUrl(String url) {
        String after = url.substring(url.lastIndexOf("/") + 1);
        return after.split("\\?")[0];
    }
}