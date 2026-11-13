@echo off
setlocal enabledelayedexpansion

title NEXUS ARENA - E-SPORTS TOURNAMENT SUITE

echo ===============================================================================
echo                ⚡ NEXUS ARENA - E-SPORTS TOURNAMENT MANAGER ⚡
echo                       Full-Stack Java 21 & Firebase Cloud
echo ===============================================================================
echo.

:: 1. Check if Java 21 is available
set "JAVA_CMD=java"
where java >nul 2>&1
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe"
    ) else (
        echo [INFO] Java 21 was not detected on this system.
        echo [AUTO-SETUP] Installing Eclipse Temurin JDK 21 automatically via Windows Package Manager...
        winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-source-agreements --accept-package-agreements
        
        if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe" (
            set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe"
        ) else (
            echo.
            echo [ERROR] Auto-install failed or requires manual installation.
            echo Please download Java 21 from: https://adoptium.net
            pause
            exit /b 1
        )
    )
)

set "BACKEND_JAR=backend-1.0.0.jar"
if not exist "%BACKEND_JAR%" (
    if exist "backend\target\backend-1.0.0.jar" (
        set "BACKEND_JAR=backend\target\backend-1.0.0.jar"
    )
)

set "DESKTOP_JAR=desktop-1.0.0.jar"
if not exist "%DESKTOP_JAR%" (
    if exist "desktop\target\desktop-1.0.0.jar" (
        set "DESKTOP_JAR=desktop\target\desktop-1.0.0.jar"
    )
)

echo [1/3] Starting Spring Boot Backend and Cloud Sync Engine (Port 8080)...
start "Nexus Arena Backend & Cloud API" cmd /k "chcp 65001 >nul && "%JAVA_CMD%" -jar "%BACKEND_JAR%""

echo Waiting for backend server initialization...
timeout /t 5 /nobreak >nul

echo [2/3] Opening Public Esports Web Portal...
start https://nexusarena-esports.web.app

echo [3/3] Launching Organizer Desktop Application (JavaFX Cyberpunk UI)...
start "Nexus Arena Organizer App" "%JAVA_CMD%" -jar "%DESKTOP_JAR%"

echo.
echo ===============================================================================
echo [SUCCESS] All systems operational!
echo  - Public Portal:  https://nexusarena-esports.web.app
echo  - Local Web:      http://localhost:8080
echo  - REST API:       http://localhost:8080/api/tournaments
echo  - Cloud DB:       Google Cloud Firestore (nexusarena-esports)
echo  - Desktop Client: Running on screen
echo ===============================================================================
echo.
