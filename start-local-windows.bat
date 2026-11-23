@echo off
cd /d "%~dp0"
echo Starting SkillSwap in two separate windows...
start "SkillSwap Java Backend" cmd /k call "%~dp0start-backend-windows.bat"
start "SkillSwap Next Frontend" cmd /k call "%~dp0start-frontend-windows.bat"
echo.
echo Check API: http://localhost:8080/api/health
echo Website: http://localhost:3000
echo Initial startup requires internet to download npm/Maven dependencies.
