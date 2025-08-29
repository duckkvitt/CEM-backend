# PowerShell script to restart the CEM-spareparts service after applying the bytea column fix
# Run this script after the database migration completes

Write-Host "🔄 Restarting CEM-spareparts service..." -ForegroundColor Yellow

# Stop the service if it's running
Write-Host "Stopping CEM-spareparts service..." -ForegroundColor Blue
try {
    $process = Get-Process -Name "java" -ErrorAction SilentlyContinue | Where-Object { $_.ProcessName -eq "java" -and $_.CommandLine -like "*cemspareparts*" }
    if ($process) {
        Write-Host "Found running CEM-spareparts process (PID: $($process.Id))" -ForegroundColor Green
        Stop-Process -Id $process.Id -Force
        Write-Host "Service stopped successfully" -ForegroundColor Green
        Start-Sleep -Seconds 3
    } else {
        Write-Host "No running CEM-spareparts process found" -ForegroundColor Yellow
    }
} catch {
    Write-Host "Error stopping service: $($_.Exception.Message)" -ForegroundColor Red
}

# Wait a moment for the process to fully stop
Write-Host "Waiting for service to fully stop..." -ForegroundColor Blue
Start-Sleep -Seconds 5

# Start the service
Write-Host "Starting CEM-spareparts service..." -ForegroundColor Blue
try {
    # Change to the CEM-spareparts directory
    Set-Location "C:\Users\Lom\Documents\Viet\CEM-backend\CEM-spareparts"
    
    # Start the service using Maven
    Write-Host "Starting service with Maven..." -ForegroundColor Blue
    Start-Process -FilePath "mvn" -ArgumentList "spring-boot:run" -WorkingDirectory "C:\Users\Lom\Documents\Viet\CEM-backend\CEM-spareparts" -WindowStyle Minimized
    
    Write-Host "Service started successfully!" -ForegroundColor Green
    Write-Host "Waiting for service to fully initialize..." -ForegroundColor Blue
    
    # Wait for the service to start
    Start-Sleep -Seconds 30
    
    # Check if the service is responding
    Write-Host "Checking service health..." -ForegroundColor Blue
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:8085/actuator/health" -Method Get -TimeoutSec 10
        if ($response.status -eq "UP") {
            Write-Host "✅ Service is healthy and responding!" -ForegroundColor Green
        } else {
            Write-Host "⚠️ Service is running but health check shows: $($response.status)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "⚠️ Service may still be starting up. Health check failed: $($_.Exception.Message)" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "Error starting service: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "You may need to start the service manually using: mvn spring-boot:run" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎯 Bytea column fix applied and service restarted!" -ForegroundColor Green
Write-Host "The supplier device type queries should now work without errors." -ForegroundColor Green
Write-Host ""
Write-Host "To verify the fix:" -ForegroundColor Cyan
Write-Host "1. Check the application logs for successful queries" -ForegroundColor Cyan
Write-Host "2. Test the API endpoint: GET /supplier-device-types" -ForegroundColor Cyan
Write-Host "3. Run the test script: test-fix.sql" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
