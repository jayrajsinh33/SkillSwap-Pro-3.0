@echo off
setlocal
cd /d "%~dp0frontend"
where npm.cmd >nul 2>&1
if errorlevel 1 (echo ERROR: Node.js and npm are required. & pause & exit /b 1)
if not exist "node_modules\next\package.json" (
  echo Installing frontend dependencies for first run...
  call npm.cmd install
  if errorlevel 1 (echo npm installation failed. & pause & exit /b 1)
)
echo Starting SkillSwap on http://localhost:3000 ...
call npm.cmd run dev
if errorlevel 1 echo Frontend did not start. Read the error above.
pause
