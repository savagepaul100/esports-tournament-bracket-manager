@echo off
setlocal enabledelayedexpansion

title NEXUS ARENA - BACKEND & PUBLIC PORTAL

where java >nul 2>&1
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
        set "PATH=!JAVA_HOME!\bin;!PATH!"
    )
)

echo Starting Spring Boot Backend on http://localhost:8080 ...
java -jar backend\target\backend-1.0.0.jar
pause
