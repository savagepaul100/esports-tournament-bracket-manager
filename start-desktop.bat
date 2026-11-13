@echo off
setlocal enabledelayedexpansion

title NEXUS ARENA - ORGANIZER DESKTOP SUITE

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

set "DESKTOP_JAR=desktop-1.0.0.jar"
if not exist "%DESKTOP_JAR%" (
    if exist "desktop\target\desktop-1.0.0.jar" (
        set "DESKTOP_JAR=desktop\target\desktop-1.0.0.jar"
    )
)

echo ===============================================================================
echo                ⚡ NEXUS ARENA - ORGANIZER DESKTOP CLIENT ⚡
echo                       Cyberpunk JavaFX & Firebase Cloud
echo ===============================================================================
echo [INFO] Launching Nexus Arena Desktop Application...
"%JAVA_CMD%" -jar "%DESKTOP_JAR%"
if %errorlevel% neq 0 (
    pause
)
