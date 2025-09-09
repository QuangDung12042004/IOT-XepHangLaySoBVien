@echo off
echo =====================================
echo HOSPITAL QUEUE API TESTING SCRIPT
echo =====================================
echo.

echo 1. Testing Departments API...
echo GET /api/departments
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/departments' -Method GET; $response | ConvertTo-Json -Depth 3 } catch { Write-Host 'Error:' $_.Exception.Message }"
echo.

echo 2. Testing Hardware Ping...
echo GET /api/hardware/ping
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/hardware/ping' -Method GET; $response | ConvertTo-Json -Depth 3 } catch { Write-Host 'Error:' $_.Exception.Message }"
echo.

echo 3. Testing Dashboard Overview...
echo GET /api/dashboard/overview
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/dashboard/overview' -Method GET; $response | ConvertTo-Json -Depth 3 } catch { Write-Host 'Error:' $_.Exception.Message }"
echo.

echo 4. Testing Take Number (K01)...
echo POST /api/tickets/take-number
powershell -Command "try { $body = @{departmentCode='K01'} | ConvertTo-Json; $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/tickets/take-number' -Method POST -Body $body -ContentType 'application/json'; $response | ConvertTo-Json -Depth 3 } catch { Write-Host 'Error:' $_.Exception.Message }"
echo.

echo 5. Testing Waiting Queue for K01...
echo GET /api/tickets/waiting/1
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/api/tickets/waiting/1' -Method GET; $response | ConvertTo-Json -Depth 3 } catch { Write-Host 'Error:' $_.Exception.Message }"
echo.

echo =====================================
echo TESTING COMPLETED
echo =====================================
pause
