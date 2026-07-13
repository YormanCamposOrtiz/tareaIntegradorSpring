# Verifica Prometheus, Grafana y jobs del backend Spring
# Uso: .\scripts\verificar-monitoreo.ps1

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$PrometheusUrl = "http://localhost:9090",
    [string]$GrafanaUrl = "http://localhost:3000"
)

function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Url,
        [scriptblock]$Validator
    )

    Write-Host "`n==> $Name" -ForegroundColor Cyan
    try {
        $response = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 10
        $content = $response.Content
        & $Validator $content $response.StatusCode
        Write-Host "OK ($($response.StatusCode))" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "ERROR: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

$results = @()

$results += Test-Endpoint -Name "Health Actuator" -Url "$BaseUrl/actuator/health" -Validator {
    param($content)
    if ($content -notmatch '"status"\s*:\s*"UP"') {
        throw "Health no esta UP"
    }
}

$results += Test-Endpoint -Name "Prometheus endpoint" -Url "$BaseUrl/actuator/prometheus" -Validator {
    param($content)
    if ($content -notmatch "jvm_memory_used_bytes") {
        throw "No se encontraron metricas JVM"
    }
    if ($content -notmatch "paginaweb_productos_total") {
        Write-Host "Aviso: aun no aparece paginaweb_productos_total. Espera 1 min al job fixed-rate." -ForegroundColor Yellow
    }
}

$results += Test-Endpoint -Name "Estado de jobs" -Url "$BaseUrl/api/jobs/status" -Validator {
    param($content)
    if ($content -notmatch "actualizar-metricas") {
        Write-Host "Aviso: el job fixed-rate aun no ejecuto. Espera hasta 60 segundos." -ForegroundColor Yellow
    }
}

$results += Test-Endpoint -Name "Prometheus server targets" -Url "$PrometheusUrl/api/v1/targets" -Validator {
    param($content)
    if ($content -notmatch '"health"\s*:\s*"up"') {
        throw "Prometheus no tiene targets UP. Levanta docker-compose y el backend."
    }
}

$results += Test-Endpoint -Name "Grafana login page" -Url "$GrafanaUrl/login" -Validator {
    param($content)
    if ($content -notmatch "Grafana") {
        throw "Grafana no responde correctamente"
    }
}

Write-Host "`n====================" -ForegroundColor Cyan
$ok = ($results | Where-Object { $_ -eq $true }).Count
$total = $results.Count
Write-Host "Resultado: $ok / $total verificaciones OK" -ForegroundColor $(if ($ok -eq $total) { "Green" } else { "Yellow" })

Write-Host "`nComandos utiles:" -ForegroundColor Cyan
Write-Host "  Backend:   mvn spring-boot:run"
Write-Host "  Monitoreo: docker compose -f docker-compose.monitoring.yml up -d"
Write-Host "  Grafana:   $GrafanaUrl (admin / admin123)"
Write-Host "  Prometheus:$PrometheusUrl"

if ($ok -lt $total) {
    exit 1
}
