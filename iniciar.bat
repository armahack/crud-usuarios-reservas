@echo off
setlocal
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0iniciar.ps1"
set "EXIT_CODE=%ERRORLEVEL%"
if not "%EXIT_CODE%"=="0" (
    echo.
    echo No se pudo iniciar la aplicacion. Revisa el mensaje anterior.
    pause
)
exit /b %EXIT_CODE%
