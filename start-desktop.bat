@echo off
setlocal enabledelayedexpansion

title NEXUS ARENA - ORGANIZER DESKTOP

where java >nul 2>&1
if %errorlevel% neq 0 (
    if exist "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe" (
        set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
        set "PATH=!JAVA_HOME!\bin;!PATH!"
    )
)

echo Launching JavaFX Organizer Desktop Client...
java -jar desktop\target\desktop-1.0.0.jar
pause
