@echo off
echo ========================================
echo EJECUTANDO TESTS DEL MÓDULO PRODUCCIÓN
echo ========================================
echo.

call mvnw.cmd test -Dtest=ProduccionServiceTest

echo.
echo ========================================
echo TESTS FINALIZADOS
echo ========================================
pause
