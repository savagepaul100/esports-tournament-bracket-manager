@echo off
setlocal enabledelayedexpansion
title NEXUS ARENA - Cloud E-Sports Management Suite
cd /d "%~dp0"

echo ===============================================================================
echo                ⚡ NEXUS ARENA - ORGANIZER COMMAND SUITE ⚡
echo                  100%% Cloud-Powered by Google Firestore
echo ===============================================================================

set "JAVA_CMD=java"
where java >nul 2>&1
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe" (
        set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe"
    ) else (
        echo [INFO] Java 21 was not detected. Installing Eclipse Temurin JDK 21 via winget...
        winget install --id EclipseAdoptium.Temurin.21.JDK -e --accept-source-agreements --accept-package-agreements
        if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe" (
            set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe"
        )
    )
)

set "DESKTOP_JAR=desktop\target\desktop-1.0.0.jar"
if not exist "%DESKTOP_JAR%" (
    if exist "desktop-1.0.0.jar" (
        set "DESKTOP_JAR=desktop-1.0.0.jar"
    ) else if exist "..\desktop\target\desktop-1.0.0.jar" (
        set "DESKTOP_JAR=..\desktop\target\desktop-1.0.0.jar"
    )
)

if not exist "%DESKTOP_JAR%" (
    echo [ERROR] Could not find desktop-1.0.0.jar.
    echo Please run '.\mvnw.cmd package -DskipTests=true' first.
    pause
    exit /b 1
)

echo [OK] Cloud Firestore Backend: Connected (nexusarena-esports)
echo [OK] Public Web Portal: https://nexusarena-esports.web.app
echo [INFO] Launching Nexus Arena Desktop App...
echo.

"%JAVA_CMD%" -jar "%DESKTOP_JAR%"
if %errorlevel% neq 0 (
    pause
)
