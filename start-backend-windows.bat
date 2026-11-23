@echo off
setlocal
cd /d "%~dp0backend"
where java >nul 2>&1
if errorlevel 1 (echo ERROR: Java 21 not found on PATH. & pause & exit /b 1)
set "MVN="
where mvn.cmd >nul 2>&1
if not errorlevel 1 set "MVN=mvn.cmd"
if not defined MVN if exist "C:\Tools\apache-maven\apache-maven-3.10.0\bin\mvn.cmd" set "MVN=C:\Tools\apache-maven\apache-maven-3.10.0\bin\mvn.cmd"
if not defined MVN if exist "C:\Tools\apache-maven\bin\mvn.cmd" set "MVN=C:\Tools\apache-maven\bin\mvn.cmd"
if not defined MVN (echo ERROR: Maven was not found. Edit this file with the path to mvn.cmd. & pause & exit /b 1)
echo Starting SkillSwap Spring Boot API on http://localhost:8080 ...
call "%MVN%" spring-boot:run
if errorlevel 1 echo Backend did not start. Read Maven errors above.
pause
